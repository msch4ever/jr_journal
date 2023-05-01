package cz.los.jr_journal.bot.handler;

import cz.los.jr_journal.bot.BotResponse;
import cz.los.jr_journal.bot.command.Command;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;
import java.util.Optional;

@Slf4j
public class RootCommandHandler extends AbstractCommandHandler implements CommandHandler<SendMessage> {

    private final Map<Command, CommandHandler> handlersRegistry;
    private final ErrorHandler errorHandler;

    public RootCommandHandler(Map<Command, CommandHandler> handlersRegistry, ErrorHandler errorHandler) {
        this.handlersRegistry = handlersRegistry;
        this.errorHandler = errorHandler;
    }

    @Override
    public BotResponse<SendMessage> handle(Update update) {
        Optional<Command> inputCommand = extractCommand(update);
        return handlersRegistry.getOrDefault(inputCommand.get(), errorHandler).handle(update);
    }

}
