package cz.los.jr_journal.utils;

import cz.los.jr_journal.bot.command.Command;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public final class ValidationUtils {

    private static final String VALUE = "value";
    public static final String COMMAND = "command";

    private ValidationUtils() {}

    public static void validateNotNull(Object o) {
        validateNotNull(o, VALUE);
    }
    public static void validateNotNull(Object o, String what) {
        if (!Objects.nonNull(o)) {
            String message = String.format("Provided %s should not be null!", what);
            log.error(message);
            throw new IllegalArgumentException(message);
        }
    }

    public static void validateCommand(Command provided, Command expected) {
        validateNotNull(provided, COMMAND);
        validateNotNull(expected, COMMAND);
        if (expected != provided) {
            String message = String.format(
                    "Wrong command! Expected [%s], provided [%s]", expected.getCommand(), provided.getCommand());
            log.error(message);
            throw new IllegalArgumentException(message);
        }
    }

}
