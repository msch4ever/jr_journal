package cz.los.jr_journal.bot.handler;

import cz.los.jr_journal.bot.BotResponse;
import cz.los.jr_journal.bot.conversation.AssignMentorConversation;
import cz.los.jr_journal.bot.conversation.Conversation;
import cz.los.jr_journal.bot.conversation.ConversationKeeper;
import cz.los.jr_journal.model.BotUser;
import cz.los.jr_journal.model.Group;
import cz.los.jr_journal.service.GroupService;
import cz.los.jr_journal.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;

import static cz.los.jr_journal.bot.command.Command.ASSIGN_MENTOR;
import static cz.los.jr_journal.bot.command.Command.NEW_GROUP;

@Slf4j
public class AssignMentorHandler extends AbstractCommandHandler implements CommandHandler {


    private static final String ENTER_GROUP_NAME = "Введите имя для группы";
    private static final String ENTER_MENTOR_NAME = "Введите логин метнора чтобы привязать его к группе";
    private static final String ASSIGN_SELF = """
            Хочешь привязать себя к этой группе?
            "+" - подтвердить
            "-" - отменить
            """;
    private static final String CONFIRM_ASSIGNMENT = """
            Подтверди привязку *%s к группе %s*!
            "+" - подтвердить
            "-" - отменить
            """;
    private static final String ASSIGNMENT_COMPLETED = "Привязка *%s к группе %s* прошла успешно";
    private static final String GROUP_DOES_NOT_EXIST = "Такой группы не существует. Может для начала ее создать?";

    private final GroupService groupService;
    private final UserService userService;
    private final ConversationKeeper keeper;

    public AssignMentorHandler(GroupService groupService, UserService userService, ConversationKeeper keeper) {
        this.groupService = groupService;
        this.userService = userService;
        this.keeper = keeper;
    }

    @Override
    public BotResponse<SendMessage> handle(Update update) {
        log.info("Handling assign mentor command. {}", update);
        Message message = update.getMessage();
        if (message.isCommand()) {
            return handleFirstStep(message, ASSIGN_MENTOR, ENTER_GROUP_NAME, keeper);
        }
        Long chatId = message.getChatId();
        if (keeper.conversationExists(chatId)) {
            AssignMentorConversation conversation = (AssignMentorConversation) keeper.get(chatId);
            switch (conversation.getStep()) {
                case 1: return readGroupName(message, conversation);
                case 2: return readAssignSelf(message, conversation);
                case 3: return readAssignOther(message, conversation);
                case 4: return confirmAssigment(message, conversation);
            }
        }
        log.warn("Something went wrong with command. {}", NEW_GROUP);
        keeper.remove(chatId);
        return new BotResponse<>(SendMessage.builder()
                .chatId(chatId)
                .text(OPERATION_ABORTED)
                .build());
    }

    private BotResponse<SendMessage> readGroupName(Message message, AssignMentorConversation conversation) {
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
        Optional<Group> groupOptional = groupService.findByName(input.groupName);
        if (groupOptional.isEmpty()) {
            log.warn("Group:{} does not exist. Sending error message!", input.groupName);
            return new BotResponse<>(SendMessage.builder()
                    .chatId(message.getChatId())
                    .text(GROUP_DOES_NOT_EXIST)
                    .build());
        }
        conversation.setGroup(groupOptional.get());
        conversation.incrementStep();
        log.info("Group name successfully obtained. Name:{}", input.groupName);
        return new BotResponse<>(SendMessage.builder()
                .chatId(message.getChatId())
                .text(ASSIGN_SELF)
                .build());
    }

    private BotResponse<SendMessage> readAssignSelf(Message message, AssignMentorConversation conversation) {
        log.info("Reading decision to assign creator to the group for conversation chatId:{} command:{}",
                conversation.getChatId(), conversation.getCommand());
        String commandText = message.getText();
        ConfirmInput input = new NewGroupHandler.ConfirmInput(commandText);
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
            conversation.incrementStep();
            conversation.incrementStep(); // to jump over "pick other mentor" step
            return new BotResponse<>(SendMessage.builder()
                    .chatId(message.getChatId())
                    .text(String.format(CONFIRM_ASSIGNMENT, conversation.getMentor().getUsername(),
                            conversation.getGroup().getDisplayName()))
                    .parseMode(MARKDOWN)
                    .build());
        } else {
            log.info("Sending message to provide other user login to assign to the group...");
            conversation.incrementStep();
            return new BotResponse<>(SendMessage.builder()
                    .chatId(message.getChatId())
                    .text(ENTER_MENTOR_NAME)
                    .build());
        }
    }

    private BotResponse<SendMessage> readAssignOther(Message message, AssignMentorConversation conversation) {
        log.info("Reading other mentor login to assign to the group for conversation chatId:{} command:{}",
                conversation.getChatId(), conversation.getCommand());
        String username = message.getText();
        boolean inputValid = username.split("\\s").length == 1 && username.length() > 1;
        if (!inputValid) {
            log.warn("Provided mentor name is of unexpected format. Sending error message!");
            return new BotResponse<>(SendMessage.builder()
                    .chatId(message.getChatId())
                    .text("Имя ментора не может содержать пробелов и должно быть длинне одного символа.")
                    .build());
        }
        Optional<BotUser> userOptional = userService.findByUsername(username);
        if (userOptional.isEmpty()) {
            log.warn("Could not find bot user with username:{}. Sending error message!", username);
            return new BotResponse<>(SendMessage.builder()
                    .chatId(message.getChatId())
                    .text("Не смог найти ниодного ментора с таким юзернеймом. Попробуй другой.")
                    .build());
        }
        conversation.setMentor(userOptional.get());
        log.info("Mentor found. Sending message to confirm operation...");
        conversation.incrementStep();
        return new BotResponse<>(SendMessage.builder()
                .chatId(message.getChatId())
                .text(String.format(CONFIRM_ASSIGNMENT, conversation.getMentor().getUsername(),
                        conversation.getGroup().getDisplayName()))
                .parseMode(MARKDOWN)
                .build());
    }

    private BotResponse<SendMessage> confirmAssigment(Message message, AssignMentorConversation conversation) {
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
            groupService.assignMentor(conversation);
            keeper.remove(conversation.getChatId());
            return new BotResponse<>(
                            SendMessage.builder()
                                    .chatId(message.getChatId())
                                    .text(String.format(ASSIGNMENT_COMPLETED, conversation.getMentor().getUsername(),
                                            conversation.getGroup().getDisplayName()))
                                    .parseMode("markdown")
                                    .build());
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
        return new AssignMentorConversation(chatId);
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
}
