package cz.los.jr_journal.bot.handler;

import cz.los.jr_journal.bot.BotResponse;
import cz.los.jr_journal.bot.command.Command;
import cz.los.jr_journal.bot.conversation.ConversationKeeper;
import cz.los.jr_journal.bot.conversation.ConversationKey;
import cz.los.jr_journal.bot.conversation.NewGroupConversation;
import cz.los.jr_journal.model.Group;
import cz.los.jr_journal.model.Level;
import cz.los.jr_journal.service.GroupService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.*;

import static cz.los.jr_journal.bot.command.Command.NEW_GROUP;
import static cz.los.jr_journal.bot.command.Command.NEW_LEVEL;

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
    private static final String ASSIGN_MENTOR = "Введи имя ментора или \"дальше\" чтобы пропустить этот шаг(к группе может быть приставлено не более 2х менторов)";
    private static final String CONFIRM_CREATION = """
            Подтверди создание группы *%s на уровне %s с менторами %s и %s*!
            "+" - подтвердить
            "-" - отменить
            """;
    private static final String OPERATION_ABORTED = "Операция отменена!";
    private static final String GROUP_CREATED = "Группа *%s* успешно зарегистрирована";
    private static final String GROUP_ALREADY_EXISTS = "Группа *%s* уже существует.. Проверь, может это ошибка?";
    private static final String WRONG_CONVERSATION_STATE = "Произошла ошибка в комманде регистрации группы. Начнем заново?";
    private final GroupService groupService;
    private final ConversationKeeper keeper;

    public NewGroupHandler(GroupService groupService, ConversationKeeper keeper) {
        this.groupService = groupService;
        this.keeper = keeper;
    }

    @Override
    public BotResponse<SendMessage> handle(Update update) {
        log.info("Handling group creation command. {}", update);
        Message message = update.getMessage();
        if (message.isCommand()) {
            return handleFirstStep(message);
        }
        Long chatId = message.getChatId();
        if (keeper.conversationExists(chatId)) {
            NewGroupConversation conversation = (NewGroupConversation) keeper.get(chatId);
            switch (conversation.getStep()) {
                case 1: return readName(message, conversation);
                case 2: return readLevel(message, conversation);
                /*case 3:
                    return readMentor(message, conversation);*/
                case 3: return confirmCreation(message, conversation);
            }
        }
        log.warn("Something went wrong with command. {}", NEW_GROUP);
        keeper.remove(chatId);
        return new BotResponse<>(SendMessage.builder()
                .chatId(chatId)
                .text(OPERATION_ABORTED)
                .build());
    }

    private BotResponse<SendMessage> handleFirstStep(Message message) {
        log.info("Trying to handle first step...");
        Optional<Command> command = extractCommand(message);
        if (command.stream().anyMatch(it -> it == NEW_GROUP)) {
            NewGroupConversation conversation = new NewGroupConversation(message.getChatId());
            if (keeper.add(conversation)) {
                conversation.incrementStep();
                log.info("First step successful!");
                return new BotResponse<>(SendMessage.builder()
                        .chatId(message.getChatId())
                        .text(ENTER_NAME)
                        .build());
            }
        }
        log.warn("Command is wrong or conversation exists...");
        return new BotResponse<>(SendMessage.builder()
                .chatId(message.getChatId())
                .text(WRONG_CONVERSATION_STATE)
                .build());
    }

    private BotResponse<SendMessage> readName(Message message, NewGroupConversation conversation) {
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
                //.text(ASSIGN_MENTOR)
                .text(String.format(CONFIRM_CREATION, conversation.getGroupName(), conversation.getModule(), "Костя", "Сережа"))
                .parseMode(MARKDOWN)
                .build());
    }

    private BotResponse<SendMessage> confirmCreation(Message message, NewGroupConversation conversation) {
        log.info("Reading group creation confirmation for conversation chatId:{} command:{}", conversation.getChatId(), conversation.getCommand());
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
                                    .text(String.format(GROUP_CREATED, value.getName()))
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
            boolean levelExists = Level.getByNumber(Integer.parseInt(level)).isPresent();
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

    private static class ConfirmInput extends AbstractInput {

        private static final String NO_DECISION_PROVIDED = "Не предоставлено решение.";
        private static final String TOO_MANY_ARGUMENTS = "Предоставлено слишком много параметров. " +
                "Убедись, что решение предоставлено символом \"+\" или \"-\"";
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

        private boolean decision;

        public ConfirmInput(String commandText) {
            super(commandText);
        }

        @Override
        protected void validate() {
            switch (splitText.length) {
                case 0 -> initErrorIfLen0();
                case 1 -> checkIfDecisionIsSingleDigit();
                default -> intiErrorIfTooLong();
            }
        }

        private void initErrorIfLen0() {
            errorMessage = NO_DECISION_PROVIDED +
                    System.lineSeparator() +
                    NEW_LEVEL.getDescription();
            valid = false;
        }

        private void checkIfDecisionIsSingleDigit() {
            String decision = splitText[0];
            boolean decisionIsSingleDigit = decision.length() == 1;
            if (!decisionIsSingleDigit) {
                errorMessage = TOO_MANY_ARGUMENTS +
                        System.lineSeparator() +
                        NEW_LEVEL.getDescription();
                valid = false;
            } else {
                valid = "+".equals(decision) || "-".equals(decision);
            }
        }

        private void intiErrorIfTooLong() {
            errorMessage = TOO_MANY_ARGUMENTS +
                    System.lineSeparator() +
                    NEW_LEVEL.getDescription();
            valid = false;
        }

        @Override
        protected void extractThisParams() {
            this.decision = "+".equals(splitText[0]);
        }
    }
}
