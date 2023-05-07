package cz.los.jr_journal.bot.handler;

import cz.los.jr_journal.bot.BotResponse;
import cz.los.jr_journal.bot.command.Command;
import cz.los.jr_journal.bot.conversation.Conversation;
import cz.los.jr_journal.bot.conversation.ConversationKeeper;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static cz.los.jr_journal.bot.command.Command.NEW_LEVEL;

@Slf4j
public abstract class AbstractCommandHandler {

    protected static final String MARKDOWN = "markdown";
    private static final String WRONG_CONVERSATION_STATE = "Произошла ошибка в комманде регистрации группы. Начнем заново?";
    protected static final String OPERATION_ABORTED = "Операция отменена!";

    protected static final String STICK_TO_THE_RULES = "Придерживайся корректоной формы команды.";
    protected static final String FATAL_ERROR = "*Произошла чудовищная ошибка.* Я хер знает что там не так:) "
            + STICK_TO_THE_RULES;

    protected Optional<Command> extractCommand(Update update) {
        return extractCommand(update.getMessage());
    }

    protected Optional<Command> extractCommand(Message message) {
        String text = message.getText().trim().toLowerCase();
        List<Command> present = Arrays.stream(Command.values())
                .filter(it -> text.contains(it.getCommand()))
                .toList();
        if (present.size() != 1) {
            log.warn("Could not find command in command message!");
            return Optional.empty();
        }
        return Optional.of(present.get(0));
    }

    protected BotResponse<SendMessage> handleFirstStep(Message message, Command expectedCommand, String firstMessage,
                                                       ConversationKeeper keeper) {
        log.info("Trying to handle first step...");
        Optional<Command> command = extractCommand(message);
        if (command.stream().anyMatch(it -> it == expectedCommand)) {
            Conversation conversation = createConversation(message.getChatId());
            if (keeper.add(conversation)) {
                conversation.incrementStep();
                log.info("First step successful!");
                return new BotResponse<>(SendMessage.builder()
                        .chatId(message.getChatId())
                        .text(firstMessage)
                        .build());
            }
        }
        log.warn("Command is wrong or conversation exists...");
        return new BotResponse<>(SendMessage.builder()
                .chatId(message.getChatId())
                .text(WRONG_CONVERSATION_STATE)
                .build());
    }

    protected Conversation createConversation(long chatId) {
        throw new RuntimeException("createConversation method should be overridden to be used.");
    }

    protected static abstract class AbstractInput {

        protected boolean valid;
        protected String errorMessage;
        protected final String[] splitText;

        protected AbstractInput(String commandText) {
            this.splitText = commandText.split("\\s");
        }

        protected abstract void validate();

        protected void extractParams() {
            if (!valid) {
                String message = "extract params method cannot be called on invalid input!";
                log.error(message);
                throw new IllegalStateException(message);
            }
            extractThisParams();
        }

        protected abstract void extractThisParams();
    }

    protected static class ConfirmInput extends AbstractInput {

        private static final String NO_DECISION_PROVIDED = "Не предоставлено решение.";
        private static final String ENFORCE_DECISION_FORMAT = "Убедись, что решение предоставлено символом \"+\" или \"-\"";
        private static final String TOO_MANY_ARGUMENTS = "Предоставлено слишком много параметров. " +
                ENFORCE_DECISION_FORMAT;

        protected boolean decision;

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
            } else if ("+".equals(decision) || "-".equals(decision)) {
                valid = true;
            } else {
                errorMessage = NO_DECISION_PROVIDED +
                        System.lineSeparator() +
                        ENFORCE_DECISION_FORMAT;
                valid = false;
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
