package cz.los.jr_journal.bot.handler;

import cz.los.jr_journal.bot.BotResponse;
import cz.los.jr_journal.bot.conversation.Conversation;
import cz.los.jr_journal.bot.conversation.ConversationKeeper;
import cz.los.jr_journal.bot.conversation.NewGroupConversation;
import cz.los.jr_journal.model.BotUser;
import cz.los.jr_journal.model.Group;
import cz.los.jr_journal.model.Level;
import cz.los.jr_journal.service.GroupService;
import cz.los.jr_journal.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.*;

import static cz.los.jr_journal.bot.command.Command.*;

@Slf4j
public class NewGroupHandler extends AbstractCommandHandler implements CommandHandler<SendMessage> {

    private static final String ENTER_NAME = "Введите имя для группы";
    private static final String ENTER_LEVEL = """
            Введите уровень для группы *%s* от 1 до 6
            Уровней у нас всего шесть.
                1 - Java Syntax
                2 - Java Core
                3 - Java Professional
                4 - Работа с БД
                5 - Spring
                6 - Итоговый проект
            """;
    private static final String ASSIGN_SELF = """
            Хочешь привязать себя к этой группе?
            "+" - подтвердить
            "-" - отменить
            """;
    private static final String CONFIRM_CREATION = """
            Подтверди создание группы *%s на уровне %s*!
            "+" - подтвердить
            "-" - отменить
            """;
    private static final String GROUP_CREATED = "Группа *%s* успешно зарегистрирована";
    private static final String GROUP_ALREADY_EXISTS = "Группа *%s* уже существует.. Проверь, может это ошибка?";
    private final GroupService groupService;
    private final UserService userService;
    private final ConversationKeeper keeper;

    public NewGroupHandler(GroupService groupService, UserService userService, ConversationKeeper keeper) {
        this.groupService = groupService;
        this.userService = userService;
        this.keeper = keeper;
    }

    @Override
    public BotResponse<SendMessage> handle(Update update) {
        log.info("Handling group creation command. {}", update);
        Message message = update.getMessage();
        if (message.isCommand()) {
            return handleFirstStep(message, NEW_GROUP, ENTER_NAME, keeper);
        }
        Long chatId = message.getChatId();
        if (keeper.conversationExists(chatId)) {
            NewGroupConversation conversation = (NewGroupConversation) keeper.get(chatId);
            switch (conversation.getStep()) {
                case 1: return readGroupName(message, conversation);
                case 2: return readLevel(message, conversation);
                case 3: return readAssignSelf(message, conversation);
                case 4: return confirmCreation(message, conversation);
            }
        }
        log.warn("Something went wrong with command. {}", NEW_GROUP);
        keeper.remove(chatId);
        return new BotResponse<>(SendMessage.builder()
                .chatId(chatId)
                .text(OPERATION_ABORTED)
                .build());
    }

    private BotResponse<SendMessage> readGroupName(Message message, NewGroupConversation conversation) {
        log.info("Reading group name for conversation key:{}", conversation.getChatId());
        String commandText = message.getText();
        GroupNameInput input = new GroupNameInput(commandText);
        input.validate();
        if (!input.valid) {
            log.warn("Could not read name. Sending error message!");
            return new BotResponse<>(SendMessage.builder()
                    .chatId(message.getChatId())
                    .text(input.errorMessage)
                    .build());
        }
        input.extractParams();
        conversation.setGroupName(input.groupName);
        conversation.incrementStep();
        log.info("Group name successfully obtained. Name:{}", input.groupName);
        return new BotResponse<>(SendMessage.builder()
                .chatId(message.getChatId())
                .text(String.format(ENTER_LEVEL, conversation.getGroupName()))
                .parseMode(MARKDOWN)
                .build());
    }

    private BotResponse<SendMessage> readLevel(Message message, NewGroupConversation conversation) {
        log.info("Reading level for conversation chatId:{} command:{}", conversation.getChatId(), conversation.getCommand());
        String commandText = message.getText();
        LevelInput input = new LevelInput(commandText);
        input.validate();
        if (!input.valid) {
            log.warn("Could not read level. Sending error message!");
            return new BotResponse<>(SendMessage.builder()
                    .chatId(message.getChatId())
                    .text(input.errorMessage)
                    .build());
        }
        input.extractParams();
        conversation.setModule(Level.getByNumber(input.level).get());
        conversation.incrementStep();
        log.info("Group level successfully obtained. Name:{} Level:{}", conversation.getGroupName(), conversation.getModule());
        return new BotResponse<>(SendMessage.builder()
                .chatId(message.getChatId())
                .text(ASSIGN_SELF)
                .parseMode(MARKDOWN)
                .build());
    }

    private BotResponse<SendMessage> readAssignSelf(Message message, NewGroupConversation conversation) {
        log.info("Reading decision to assign creator to the group for conversation chatId:{} command:{}",
                conversation.getChatId(), conversation.getCommand());
        String commandText = message.getText();
        ConfirmInput input = new ConfirmInput(commandText);
        input.validate();
        if (!input.valid) {
            log.warn("Could not read confirmation. Sending error message!");
            return new BotResponse<>(SendMessage.builder()
                    .chatId(message.getChatId())
                    .text(input.errorMessage)
                    .build());
        }
        input.extractParams();
        if (input.decision) {
            Optional<BotUser> userOptional = userService.findUserByTelegramId(message.getFrom().getId());
            BotUser user = userOptional.orElseGet(() -> {
                log.info("User was not registered yet. Registering new user...");
                return userService.createUser(message).get();
            });
            conversation.setMentor(user);
        } else {
            log.info("Moving on without assigning user to group...");
        }
        conversation.incrementStep();
        return new BotResponse<>(SendMessage.builder()
                .chatId(message.getChatId())
                .text(String.format(CONFIRM_CREATION, conversation.getGroupName(), conversation.getModule()))
                .parseMode(MARKDOWN)
                .build());
    }

    private BotResponse<SendMessage> confirmCreation(Message message, NewGroupConversation conversation) {
        log.info("Reading mentor assignment confirmation for conversation chatId:{} command:{}",
                conversation.getChatId(), conversation.getCommand());
        String commandText = message.getText();
        ConfirmInput input = new ConfirmInput(commandText);
        input.validate();
        if (!input.valid) {
            log.warn("Could not read confirmation. Sending error message!");
            return new BotResponse<>(SendMessage.builder()
                    .chatId(message.getChatId())
                    .text(input.errorMessage)
                    .build());
        }
        input.extractParams();
        if (input.decision) {
            Optional<Group> group = groupService.createGroup(conversation);
            keeper.remove(conversation.getChatId());
            return group.map(value -> new BotResponse<>(
                            SendMessage.builder()
                                    .chatId(message.getChatId())
                                    .text(String.format(GROUP_CREATED, value.getDisplayName()))
                                    .parseMode("markdown")
                                    .build())
                    )
                    .orElseGet(() -> new BotResponse<>(SendMessage.builder()
                            .chatId(message.getChatId())
                            .text(String.format(GROUP_ALREADY_EXISTS, conversation.getGroupName()))
                            .parseMode("markdown")
                            .build()));
        } else {
            keeper.remove(conversation.getChatId());
            return new BotResponse<>(SendMessage.builder()
                    .chatId(message.getChatId())
                    .text(OPERATION_ABORTED)
                    .build());
        }
    }

    @Override
    protected Conversation createConversation(long chatId) {
        return new NewGroupConversation(chatId);
    }

    private static class GroupNameInput extends AbstractInput {

        private static final String NO_GROUP_NAME_PROVIDED = "Название группыне не предоставлено . " + STICK_TO_THE_RULES;
        private static final String TOO_MANY_ARGUMENTS = "Предоставленно слишком много параметров." +
                "Убедись, что название группы состоит из одного слова или не содержит пробелов. " + STICK_TO_THE_RULES;
        private String groupName;

        private GroupNameInput(String commandText) {
            super(commandText);
        }

        @Override
        protected void validate() {
            if (splitText.length == 0) {
                errorMessage = NO_GROUP_NAME_PROVIDED +
                        System.lineSeparator() +
                        NEW_GROUP.getDescription();
                valid = false;
                return;
            }
            if (splitText.length > 1) {
                errorMessage = TOO_MANY_ARGUMENTS +
                        System.lineSeparator() +
                        NEW_GROUP.getDescription();
                valid = false;
                return;
            }
            valid = true;
        }

        @Override
        protected void extractThisParams() {
            this.groupName = splitText[0];
        }
    }

    private static class LevelInput extends AbstractInput {

        private static final String NO_LEVEL_PROVIDED = "Не предоставлен уровень для группы. " + STICK_TO_THE_RULES;
        private static final String TOO_MANY_ARGUMENTS = "Предоставлено слишком много параметров. " +
                "Убедись, что уроень указан цифрой от 1 до 6. " + STICK_TO_THE_RULES;
        private static final String LEVEL_DOES_NOT_EXISTS = """
                Уровней у нас всего шесть.
                1 - Java Syntax
                2 - Java Core
                3 - Java Professional
                4 - Работа с БД
                5 - Spring
                6 - Итоговый проект
                            
                А от тебя мне пришло \"%s\"... не понятно :(
                """;
        private int level;

        public LevelInput(String commandText) {
            super(commandText);
        }

        @Override
        protected void validate() {
            switch (splitText.length) {
                case 0 -> initErrorIfLen0();
                case 1 -> checkIfLevelNotSingleDigit();
                default -> intiErrorIfTooLong();
            }
        }

        private void initErrorIfLen0() {
            errorMessage = NO_LEVEL_PROVIDED +
                    System.lineSeparator() +
                    NEW_LEVEL.getDescription();
            valid = false;
        }

        private void checkIfLevelNotSingleDigit() {
            String level = splitText[0];
            boolean levelInputIsNotSingleDigit = inputIsNotSingleDigit(level);
            boolean levelExists = false;
            try {
                levelExists = Level.getByNumber(Integer.parseInt(level)).isPresent();
            } catch (Exception e) {
                log.warn("Provided level is not a number");
            }
            if (levelInputIsNotSingleDigit || !levelExists) {
                errorMessage = String.format(LEVEL_DOES_NOT_EXISTS, level) +
                        System.lineSeparator() +
                        NEW_LEVEL.getDescription();
                valid = false;
            } else {
                valid = true;
            }
        }

        private void intiErrorIfTooLong() {
            errorMessage = TOO_MANY_ARGUMENTS +
                    System.lineSeparator() +
                    NEW_LEVEL.getDescription();
            valid = false;
        }

        private static boolean inputIsNotSingleDigit(String level) {
            return level.length() != 1 || !Character.isDigit(level.charAt(0));
        }

        @Override
        protected void extractThisParams() {
            this.level = Integer.parseInt(splitText[0]);
        }
    }

}
