package cz.los.jr_journal.bot.handler;

import cz.los.jr_journal.bot.BotResponse;
import cz.los.jr_journal.model.Group;
import cz.los.jr_journal.service.GroupService;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;

import static cz.los.jr_journal.bot.command.Command.NEW_GROUP;

@Slf4j
public class NewGroupHandler extends AbstractCommandHandler implements CommandHandler<SendMessage> {

    private static final String GROUP_CREATED = "Группа *%s* успешно зарегистрирована";
    private static final String GROUP_ALREADY_EXISTS = "Группа *%s* уже существует.. Проверь, может это ошибка?";
    private final GroupService groupService;

    public NewGroupHandler(GroupService groupService) {
        this.groupService = groupService;
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
        input.extractGroupName();
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


    private static class NewGroupInput {

        private static final String STICK_TO_THE_RULES = "Придерживайся корректоной формы команды.";
        private static final String NO_GROUP_NAME_PROVIDED = "Ты не предоставил название группы. " + STICK_TO_THE_RULES;
        private static final String TOO_MANY_ARGUMENTS = "Ты предоставил слишком много параметров." +
                "Убедись, что название группы состоит из одного слова или не содержит пробелов. " + STICK_TO_THE_RULES;
        private boolean valid;
        private String errorMessage;
        private final String[] splitText;
        private String groupName;

        private NewGroupInput(String commandText) {
            this.splitText = commandText.split("\\s");
        }

        private void validate() {
            if (splitText.length == 1) {
                errorMessage = new StringBuilder()
                        .append(NO_GROUP_NAME_PROVIDED)
                        .append(System.lineSeparator())
                        .append(NEW_GROUP.getDescription())
                        .toString();
                valid = false;
                return;
            }
            if (splitText.length > 2) {
                errorMessage = new StringBuilder()
                        .append(TOO_MANY_ARGUMENTS)
                        .append(System.lineSeparator())
                        .append(NEW_GROUP.getDescription())
                        .toString();
                valid = false;
                return;
            }
            valid = true;
        }
        private void extractGroupName() {
            this.groupName = splitText[1];
        }
    }
}
