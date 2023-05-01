package cz.los.jr_journal.context;


import cz.los.jr_journal.bot.BotResponse;
import cz.los.jr_journal.bot.command.Command;
import cz.los.jr_journal.bot.handler.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static cz.los.jr_journal.bot.command.Command.START;

@Slf4j
public final class AppContext {

    private final Map<Class, Object> registry;

    private AppContext(Map<Class, Object> registry) {
        this.registry = Collections.unmodifiableMap(registry);
    }

    public static AppContext get() {
        return Holder.context;
    }

    public Object getBean(Class clazz) {
        return registry.get(clazz);
    }

    private static final class Holder {

        static {
            synchronized (Holder.class) {
                log.info("AppContext initialization started...");
                Map<Class, Object> registry = new HashMap<>();

                log.info("Initializing command handlers...");
                Map<Command, CommandHandler> handlers = new HashMap<>();
                ErrorHandler errorHandler = new ErrorHandler();
                StartHandler startHandler = new StartHandler();
                RootCommandHandler rootCommandHandler = new RootCommandHandler(handlers, errorHandler);
                MessageHandler messageHandler = new MessageHandler();
                InteractionHandler interactionHandler = new InteractionHandler(rootCommandHandler, messageHandler);
                registry.put(StartHandler.class, startHandler);
                registry.put(RootCommandHandler.class, rootCommandHandler);
                registry.put(InteractionHandler.class, interactionHandler);
                handlers.put(START, startHandler);

                context = new AppContext(registry);
                log.info("AppContext initialized!");
            }
        }
        private static AppContext context;

    }

}
