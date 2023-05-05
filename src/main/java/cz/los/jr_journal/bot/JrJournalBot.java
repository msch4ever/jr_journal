package cz.los.jr_journal.bot;

import cz.los.jr_journal.bot.config.BotConfig;
import cz.los.jr_journal.bot.handler.InteractionHandler;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


@Slf4j
public class JrJournalBot extends TelegramLongPollingBot {

    private final InteractionHandler interactionHandler;

    private final BotConfig config;

    public JrJournalBot(BotConfig config, InteractionHandler interactionHandler) {
        super(config.getToken());
        this.interactionHandler = interactionHandler;
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
        BotResponse<SendMessage> response = interactionHandler.handle(update);
        try {
            execute(response.getResponse());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
