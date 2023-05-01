package cz.los.jr_journal.bot.handler;

import cz.los.jr_journal.bot.command.Command;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
public abstract class AbstractCommandHandler {

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

}
