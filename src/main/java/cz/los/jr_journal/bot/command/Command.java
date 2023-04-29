package cz.los.jr_journal.bot.command;

public enum Command {

    START("/start"),
    REGISTER("/reg"),
    ASSIGN_MENTOR("/assign"),
    REPORT("/report");

    private String command;

    Command(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
