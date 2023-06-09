package cz.los.jr_journal.context;

import cz.los.jr_journal.bot.command.Command;
import cz.los.jr_journal.bot.conversation.ConversationGC;
import cz.los.jr_journal.bot.conversation.ConversationKeeper;
import cz.los.jr_journal.bot.handler.*;
import cz.los.jr_journal.dal.repository.GroupRepository;
import cz.los.jr_journal.dal.repository.JournalEntryRepository;
import cz.los.jr_journal.dal.repository.UserRepository;
import cz.los.jr_journal.service.GroupService;
import cz.los.jr_journal.service.JournalEntryService;
import cz.los.jr_journal.service.UserService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import static cz.los.jr_journal.bot.command.Command.*;

@Slf4j
public final class AppContext {

    @Getter
    private final Map<Class, Object> registry;

    private AppContext(Map<Class, Object> registry) {
        this.registry = registry;
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
                JournalEntryRepository journalEntryRepository = new JournalEntryRepository();

                registry.put(UserRepository.class, userRepository);
                registry.put(GroupRepository.class, groupRepository);
                registry.put(JournalEntryRepository.class, journalEntryRepository);

                log.info("Initializing services...");
                UserService userService = new UserService(userRepository, groupRepository);
                GroupService groupService = new GroupService(groupRepository);
                JournalEntryService entryService = new JournalEntryService(journalEntryRepository);

                registry.put(UserService.class, userService);
                registry.put(GroupService.class, groupService);
                registry.put(JournalEntryService.class, entryService);

                log.info("Initiating conversation Keeper...");
                ConversationKeeper keeper = new ConversationKeeper();
                ConversationGC gc = new ConversationGC(keeper);

                registry.put(ConversationKeeper.class, keeper);
                registry.put(ConversationGC.class, gc);

                log.info("Initializing command handlers...");
                Map<Command, CommandHandler> handlers = new HashMap<>();
                ErrorHandler errorHandler = new ErrorHandler();
                StartHandler startHandler = new StartHandler();
                RegisterHandler registerHandler = new RegisterHandler(userService);
                NewGroupHandler newGroupHandler = new NewGroupHandler(groupService, userService, keeper);
                NewLevelHandler newLevelHandler = new NewLevelHandler(groupService);
                AssignMentorHandler assignMentorHandler = new AssignMentorHandler(groupService, userService, keeper);
                ReportHandler reportHandler = new ReportHandler(userService, groupService, entryService, keeper);
                RootCommandHandler rootCommandHandler = new RootCommandHandler(handlers, errorHandler);
                MessageHandler messageHandler = new MessageHandler(handlers, keeper);
                InteractionHandler interactionHandler = new InteractionHandler(rootCommandHandler, messageHandler);

                registry.put(InteractionHandler.class, interactionHandler);
                registry.put(RootCommandHandler.class, rootCommandHandler);
                registry.put(StartHandler.class, startHandler);
                registry.put(RegisterHandler.class, registerHandler);
                registry.put(NewGroupHandler.class, newGroupHandler);
                registry.put(NewLevelHandler.class, newLevelHandler);
                registry.put(AssignMentorHandler.class, assignMentorHandler);
                registry.put(ReportHandler.class, reportHandler);

                handlers.put(START, startHandler);
                handlers.put(REGISTER, registerHandler);
                handlers.put(NEW_GROUP, newGroupHandler);
                handlers.put(NEW_LEVEL, newLevelHandler);
                handlers.put(ASSIGN_MENTOR, assignMentorHandler);
                handlers.put(REPORT, reportHandler);

                log.info("Initiating context...");
                context = new AppContext(registry);
                log.info("AppContext initialized!");
            }
        }
        private static AppContext context;

    }

}
