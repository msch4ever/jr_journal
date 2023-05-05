package cz.los.jr_journal.bot.handler;

import cz.los.jr_journal.bot.BotResponse;
import cz.los.jr_journal.model.BotUser;
import cz.los.jr_journal.model.Group;
import cz.los.jr_journal.model.Level;
import cz.los.jr_journal.service.GroupService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.*;

import static cz.los.jr_journal.bot.command.Command.NEW_GROUP;

@Slf4j
public class NewGroupHandler extends AbstractCommandHandler implements CommandHandler<SendMessage> {

    private static final String GROUP_CREATED = "Группа *%s* успешно зарегистрирована";
    private static final String GROUP_ALREADY_EXISTS = "Группа *%s* уже существует.. Проверь, может это ошибка?";
    private final GroupService groupService;
    private final Map<Long, NewGroupConversation> conversationMap;

    public NewGroupHandler(GroupService groupService) {
        this.groupService = groupService;
        this.conversationMap = new HashMap<>();
    }

    @Override
    public BotResponse<SendMessage> handle(Update update) {
        log.info("Handling group creation command. {}", update);
        String commandText = update.getMessage().getText();
        NewGroupInput input = new NewGroupInput(commandText);
        input.validate();
        if (!input.valid) {
            return new BotResponse<>(SendMessage.builder()
                    .chatId(update.getMessage().getChatId())
                    .text(input.errorMessage)
                    .build());
        }
        input.extractParams();
        Optional<Group> group = groupService.createGroup(input.groupName);
        return group.map(value -> new BotResponse<>(
                        SendMessage.builder()
                                .chatId(update.getMessage().getChatId())
                                .text(String.format(GROUP_CREATED, value.getName()))
                                .parseMode("markdown")
                                .build()))
                .orElseGet(() -> new BotResponse<>(SendMessage.builder()
                        .chatId(update.getMessage().getChatId())
                        .text(String.format(GROUP_ALREADY_EXISTS, input.groupName))
                        .parseMode("markdown")
                        .build()));
    }

    @Getter
    @Setter
    private static class NewGroupConversation {
        private Long chatId;
        private int step = 0;

    }


    private static class NewGroupInput extends AbstractInput {

        private static final String NO_GROUP_NAME_PROVIDED = "Название группыне не предоставлено . " + STICK_TO_THE_RULES;
        private static final String TOO_MANY_ARGUMENTS = "Предоставленно слишком много параметров." +
                "Убедись, что название группы состоит из одного слова или не содержит пробелов. " + STICK_TO_THE_RULES;
        private String groupName;

        private NewGroupInput(String commandText) {
            super(commandText);
        }

        @Override
        protected void validate() {
            if (splitText.length == 1) {
                errorMessage = NO_GROUP_NAME_PROVIDED +
                        System.lineSeparator() +
                        NEW_GROUP.getDescription();
                valid = false;
                return;
            }
            if (splitText.length > 2) {
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
            this.groupName = splitText[1];
        }
    }
}
