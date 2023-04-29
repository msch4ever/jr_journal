package cz.los.jr_journal;

import cz.los.jr_journal.bot.JrJournalBot;
import cz.los.jr_journal.bot.config.BotConfig;
import cz.los.jr_journal.bot.config.ConfigResolver;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
public class App {

    public void run(String[] cmd) throws TelegramApiException {
        log.info("Starting JR_JOURNAL bot...");
        logo();
        BotConfig config = new ConfigResolver().resolveConfig(cmd);
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(new JrJournalBot(config));
        log.info("JR_JOURNAL bot started...");
    }

    private void logo() {
        log.info("""
                       \s
                       ______          ______  __  ______  _   _____    __\s
                      / / __ \\        / / __ \\/ / / / __ \\/ | / /   |  / /\s
                 __  / / /_/ /   __  / / / / / / / / /_/ /  |/ / /| | / / \s
                / /_/ / _, _/   / /_/ / /_/ / /_/ / _, _/ /|  / ___ |/ /___
                \\____/_/ |_|____\\____/\\____/\\____/_/ |_/_/ |_/_/  |_/_____/
                          /_____/                                         \s
                """);
    }

}
