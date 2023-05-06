package cz.los.jr_journal.bot.command;

import java.util.List;

public enum Command {

    START("start", "Начать общение с ботом"),
    REGISTER("reg", "Зарегистрироваться в системе."),
    NEW_GROUP("newgroup", "Создать новую группу. Следуйте инструкциям."),
    NEW_LEVEL("newlevel", "Задать новый уровень группе. Следуйте инструкциям."),
    ASSIGN_MENTOR("assign", "Привязать метнтора к группе. Следуйте инструкциям."),
    REPORT("report", "Предоставить информацию по уроку. Следуйте инструкциям.");

    private String command;
    private String description;

    Command(String command, String description) {
        this.command = command;
        this.description = description;
    }

    public String getCommand() {
        return command;
    }

    public String getDescription() {
        return description;
    }

    public static List<Command> getInteractiveCommands() {
        return List.of(NEW_GROUP, REPORT);
    }
}
