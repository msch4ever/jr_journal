package cz.los.jr_journal.bot.handler;

import cz.los.jr_journal.bot.BotResponse;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public class MessageHandler {

    public BotResponse<SendMessage> handle(Update update) {
        return new BotResponse<>(
                SendMessage.builder()
                        .chatId(update.getMessage().getChatId())
                        .text("Я ничего не понял :(")
                        .build());
    }

}
