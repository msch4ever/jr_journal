package cz.los.jr_journal.bot.config;

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
}
