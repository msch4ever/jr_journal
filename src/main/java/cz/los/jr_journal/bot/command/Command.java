package cz.los.jr_journal.bot.command;

import java.util.List;

public enum Command {

    START("start", "Start bot"),
    REGISTER("reg", "Registers as permanent user."),
    NEW_GROUP("newgroup", "Create new Group. Usage: /newgroup <group_name>\""),
    NEW_LEVEL("newlevel", "Set new level for Group. Usage: /newlevel <group_name> <level [1, 6]\""),
    ASSIGN_MENTOR("assign", "Assign mentor to Group. Usage: /assign <group_name>"),
    REPORT("report", "Submit report about lecture.");

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
