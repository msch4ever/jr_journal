package cz.los.jr_journal.context;

import cz.los.jr_journal.bot.command.Command;
import cz.los.jr_journal.bot.handler.*;
import cz.los.jr_journal.dal.repository.GroupRepository;
import cz.los.jr_journal.dal.repository.UserRepository;
import cz.los.jr_journal.service.GroupService;
import cz.los.jr_journal.service.UserService;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static cz.los.jr_journal.bot.command.Command.*;

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

                log.info("Initializing repositories...");
                UserRepository userRepository = new UserRepository();
                GroupRepository groupRepository = new GroupRepository();

                registry.put(UserRepository.class, userRepository);
                registry.put(GroupRepository.class, groupRepository);

                log.info("Initializing services...");
                UserService userService = new UserService(userRepository);
                GroupService groupService = new GroupService(groupRepository);

                registry.put(UserService.class, userService);
                registry.put(GroupService.class, groupService);

                log.info("Initializing command handlers...");
                Map<Command, CommandHandler> handlers = new HashMap<>();
                ErrorHandler errorHandler = new ErrorHandler();
                StartHandler startHandler = new StartHandler();
                RegisterHandler registerHandler = new RegisterHandler(userService);
                NewGroupHandler newGroupHandler = new NewGroupHandler(groupService);
                RootCommandHandler rootCommandHandler = new RootCommandHandler(handlers, errorHandler);
                MessageHandler messageHandler = new MessageHandler();
                InteractionHandler interactionHandler = new InteractionHandler(rootCommandHandler, messageHandler);

                registry.put(InteractionHandler.class, interactionHandler);
                registry.put(RootCommandHandler.class, rootCommandHandler);
                registry.put(StartHandler.class, startHandler);
                registry.put(RegisterHandler.class, registerHandler);
                registry.put(NewGroupHandler.class, newGroupHandler);

                handlers.put(START, startHandler);
                handlers.put(REGISTER, registerHandler);
                handlers.put(NEW_GROUP, newGroupHandler);

                context = new AppContext(registry);
                log.info("AppContext initialized!");
            }
        }
        private static AppContext context;

    }

}
