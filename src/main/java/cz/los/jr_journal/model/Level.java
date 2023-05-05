package cz.los.jr_journal.model;

import java.util.Optional;

public enum Level {

    /**
     * KEEP ORDER OF DECLARED COMMANDS ACCORDING TO THE LEVEL NUMBER!
     */
    JAVA_SYNTAX(1), JAVA_CORE(2), JAVA_PROFESSIONAL(3), DB(4), SPRING(5), FINAL_PROJECT(6);

    final int number;

    Level(int number) {
        this.number = number;
    }

    public static Optional<Level> getByNumber(int number) {
        if (number < 1 || number > 6) {
            return Optional.empty();
        }
        return Optional.of(values()[number - 1]);
    }
}
