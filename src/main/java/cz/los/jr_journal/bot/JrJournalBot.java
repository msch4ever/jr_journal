package cz.los.jr_journal.bot;

import cz.los.jr_journal.bot.config.BotConfig;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;


@Slf4j
public class JrJournalBot extends TelegramLongPollingBot {

    private final BotConfig config;

    public JrJournalBot(BotConfig config) {
        this.config = config;
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public String getBotUsername() {
        return config.getUsername();
    }

    @Override
    public void onUpdateReceived(Update update) {
    }

}
