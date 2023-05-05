package cz.los.jr_journal.bot.handler;

import cz.los.jr_journal.bot.command.Command;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
public abstract class AbstractCommandHandler {

    protected static final String MARKDOWN = "markdown";
    protected static final String STICK_TO_THE_RULES = "Придерживайся корректоной формы команды.";
    protected static final String FATAL_ERROR = "*Произошла чудовищная ошибка.* Я хер знает что там не так:) "
            + STICK_TO_THE_RULES;

    protected Optional<Command> extractCommand(Update update) {
        String text = update.getMessage().getText().trim().toLowerCase();
        List<Command> present = Arrays.stream(Command.values())
                .filter(it -> text.contains(it.getCommand()))
                .toList();
        if (present.size() != 1) {
            log.warn("Could not find command in command message!");
            return Optional.empty();
        }
        return Optional.of(present.get(0));
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
}
