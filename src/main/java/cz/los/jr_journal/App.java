package cz.los.jr_journal;

import cz.los.jr_journal.bot.JrJournalBot;
import cz.los.jr_journal.bot.config.BotConfig;
import cz.los.jr_journal.bot.config.ConfigResolver;
import cz.los.jr_journal.bot.handler.InteractionHandler;
import cz.los.jr_journal.context.AppContext;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class App {

    public void run(String[] cmd) throws TelegramApiException {
        log.info("Starting JR_JOURNAL bot...");
        logo();
        AppContext context = AppContext.get();
        final BotConfig config = new ConfigResolver().resolveConfig(cmd);
        final JrJournalBot bot = new JrJournalBot(config, (InteractionHandler) context.getBean(InteractionHandler.class));
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(bot);
        initCommands(config, bot);
        log.info("JR_JOURNAL bot started...");
    }

    private void initCommands(BotConfig config, JrJournalBot bot) throws TelegramApiException {
        log.info("Initiating commands...");
        List<BotCommand> commands = config.provideCommands();
        log.info("{} commands present.", commands.size());
        bot.execute(SetMyCommands.builder()
                .commands(commands)
                .build());
        log.info("Following commands available:{}{}",
                System.lineSeparator(),
                commands.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(System.lineSeparator())));
        log.info("Commands initiated!");
    }

    private void logo() {
        log.info("""
                       \s
                       ______          ______  __  ______  _   _____    __\s
                      / / __ \\        / / __ \\/ / / / __ \\/ | / /   |  / /\s
                 __  / / /_/ /   __  / / / / / / / / /_/ /  |/ / /| | / / \s
                / /_/ / _, _/   / /_/ / /_/ / /_/ / _, _/ /|  / ___ |/ /___
                \\____/_/ |_|____\\____/\\____/\\____/_/ |_/_/ |_/_/  |_/_____/
                          /_____/                                         \s
                """);
    }

}
