package cz.los.jr_journal.bot.handler;

import cz.los.jr_journal.bot.BotResponse;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface CommandHandler <Method extends BotApiMethod> {

    BotResponse<Method> handle(Update update);

}
