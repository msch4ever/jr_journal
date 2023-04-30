package cz.los.jr_journal.bot.config;

import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import java.util.List;

import static cz.los.jr_journal.bot.command.Command.*;

public final class BotConfig {

    private final String botName;
    private final String username;
    private final String token;

    public BotConfig(String botName, String username, String token) {
        this.botName = botName;
        this.username = username;
        this.token = token;
    }

    public String getBotName() {
        return botName;
    }

    public String getUsername() {
        return username;
    }

    public String getToken() {
        return token;
    }

    public List<BotCommand> provideCommands() {
        return List.of(
                new BotCommand(START.getCommand(), START.getDescription()),
                new BotCommand(REGISTER.getCommand(), REGISTER.getDescription()),
                new BotCommand(NEW_GROUP.getCommand(), NEW_GROUP.getDescription()),
                new BotCommand(NEW_LEVEL.getCommand(), NEW_LEVEL.getDescription()),
                new BotCommand(ASSIGN_MENTOR.getCommand(), ASSIGN_MENTOR.getDescription()),
                new BotCommand(REPORT.getCommand(), REGISTER.getDescription())
        );
    }
}
