package cz.los.jr_journal.bot.handler;

import cz.los.jr_journal.bot.BotResponse;
import cz.los.jr_journal.model.Level;
import cz.los.jr_journal.service.GroupService;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;

import static cz.los.jr_journal.bot.command.Command.NEW_LEVEL;

@Slf4j
public class NewLevelHandler extends AbstractCommandHandler implements CommandHandler<SendMessage> {

    private static final String LEVEL_SET = "Уровень *%s* успешно установлен для группы *%s*!";
    private static final String GROUP_DOES_NOT_EXISTS = "Группа *%s* не существует.. Проверь, может это ошибка?";
    private final GroupService groupService;

    public NewLevelHandler(GroupService groupService) {
        this.groupService = groupService;
    }

    @Override
    public BotResponse<SendMessage> handle(Update update) {
        log.info("Handling set level command. {}", update);
        String commandText = update.getMessage().getText();
        SetLevelInput input = new SetLevelInput(commandText);
        input.validate();
        if (!input.valid) {
            return new BotResponse<>(SendMessage.builder()
                    .chatId(update.getMessage().getChatId())
                    .text(input.errorMessage)
                    .build());
        }
        input.extractParams();
        if (!groupService.groupExists(input.groupName)) {
            return new BotResponse<>(SendMessage.builder()
                    .chatId(update.getMessage().getChatId())
                    .text(String.format(GROUP_DOES_NOT_EXISTS, input.groupName))
                    .parseMode(MARKDOWN)
                    .build());
        }
        Optional<Level> level = groupService.setLevel(input.groupName, input.level);
        return level.map(value -> new BotResponse<>(
                        SendMessage.builder()
                                .chatId(update.getMessage().getChatId())
                                .text(String.format(LEVEL_SET, value, input.groupName))
                                .parseMode(MARKDOWN)
                                .build()))
                .orElseGet(() -> new BotResponse<>(SendMessage.builder()
                        .chatId(update.getMessage().getChatId())
                        .text(FATAL_ERROR)
                        .parseMode(MARKDOWN)
                        .build()));
    }


    private static class SetLevelInput extends AbstractInput {

        private static final String STICK_TO_THE_RULES = "Придерживайся корректоной формы команды.";
        private static final String NO_GROUP_NAME_PROVIDED = "Не предоставлено название группы. " + STICK_TO_THE_RULES;
        private static final String NO_LEVEL_PROVIDED = "Не предоставлен уровень для группы. " + STICK_TO_THE_RULES;
        private static final String TOO_MANY_ARGUMENTS = "Предоставлено слишком много параметров. " +
                "Убедись, что название группы состоит из одного слова и не содержит пробелов, а уроень указан цифрой от 1 до 6. " + STICK_TO_THE_RULES;
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
        private String groupName;
        private int level;

        public SetLevelInput(String commandText) {
            super(commandText);
        }

        @Override
        protected void validate() {
            switch (splitText.length) {
                case 1 -> initErrorIfLen1();
                case 2 -> intiErrorIfLen2();
                case 3 -> checkIfLevelNotSingleDigit();
                default -> intiErrorIfTooLong();
            }
        }

        private void initErrorIfLen1() {
            errorMessage = NO_GROUP_NAME_PROVIDED +
                    NO_LEVEL_PROVIDED +
                    System.lineSeparator() +
                    NEW_LEVEL.getDescription();
            valid = false;
        }

        private void intiErrorIfLen2() {
            if (inputIsNotSingleDigit(splitText[1])) {
                errorMessage = NO_LEVEL_PROVIDED +
                        System.lineSeparator() +
                        NEW_LEVEL.getDescription();
            } else {
                errorMessage = NO_GROUP_NAME_PROVIDED +
                        System.lineSeparator() +
                        NEW_LEVEL.getDescription();
            }
            valid = false;
        }

        private void checkIfLevelNotSingleDigit() {
            String level = splitText[2];
            boolean levelInputIsNotSingleDigit = inputIsNotSingleDigit(level);
            if (levelInputIsNotSingleDigit) {
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
            this.groupName = splitText[1];
            this.level = Integer.parseInt(splitText[2]);
        }
    }
}
