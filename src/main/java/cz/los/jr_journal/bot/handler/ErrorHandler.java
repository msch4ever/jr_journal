package cz.los.jr_journal.bot.handler;

import cz.los.jr_journal.bot.BotResponse;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
public class ErrorHandler extends AbstractCommandHandler implements CommandHandler<SendMessage> {

    @Override
    public BotResponse<SendMessage> handle(Update update) {
        log.info("Handling error. {}", update);
        return new BotResponse<>(SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text("Я не смог понять, чего ты от меня хочешь :(")
                .build());
    }
}
