package cz.los.jr_journal.bot.handler;

import cz.los.jr_journal.bot.BotResponse;
import cz.los.jr_journal.bot.command.Command;
import cz.los.jr_journal.bot.conversation.ConversationKeeper;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;

public class MessageHandler {

    private final Map<Command, CommandHandler> handlersRegistry;
    private final ConversationKeeper keeper;

    public MessageHandler(Map<Command, CommandHandler> handlersRegistry, ConversationKeeper keeper) {
        this.handlersRegistry = handlersRegistry;
        this.keeper = keeper;
    }

    public BotResponse<SendMessage> handle(Update update) {
        Long chatId = null;
        if (update.hasMessage()) {
            chatId = update.getMessage().getChatId();
            if (keeper.conversationExists(chatId)) {
                return handlersRegistry.get(keeper.get(chatId).getCommand()).handle(update);
            }
        }
        if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
            if (keeper.conversationExists(chatId)) {
                return handlersRegistry.get(keeper.get(chatId).getCommand()).handle(update);
            }
        }
        return new BotResponse<>(
                SendMessage.builder()
                        .chatId(chatId)
                        .text("Я ничего не понял :(")
                        .build());
    }

}
