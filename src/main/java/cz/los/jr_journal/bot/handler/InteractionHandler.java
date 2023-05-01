package cz.los.jr_journal.bot.handler;

import cz.los.jr_journal.bot.BotResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.MethodNotSupportedException;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

import static cz.los.jr_journal.utils.ValidationUtils.validateNotNull;

@Slf4j
public class InteractionHandler {

    private final RootCommandHandler rootCommandHandler;
    private final MessageHandler messageHandler;

    public InteractionHandler(RootCommandHandler rootCommandHandler, MessageHandler messageHandler) {
        this.rootCommandHandler = rootCommandHandler;
        this.messageHandler = messageHandler;
    }

    public BotResponse<SendMessage> handle(Update update) {
        validateNotNull(update, Update.class.toString());
        Message message = update.getMessage();
        if (message.isCommand()) {
            return rootCommandHandler.handle(update);
        } else {
            return messageHandler.handle(update);
        }
    }

    public void handle(List<Update> updates) {
        try {
            throw new MethodNotSupportedException("Working with multiple updates not implemented yet.");
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public void handle(CallbackQuery callbackQuery) {
        try {
            throw new MethodNotSupportedException("Working with callback queries not implemented yet.");
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
