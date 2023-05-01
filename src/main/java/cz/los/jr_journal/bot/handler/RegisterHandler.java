package cz.los.jr_journal.bot.handler;

import cz.los.jr_journal.bot.BotResponse;
import cz.los.jr_journal.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;


@Slf4j
public class RegisterHandler extends AbstractCommandHandler implements CommandHandler<SendMessage> {

    private final UserService userService;

    public RegisterHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public BotResponse<SendMessage> handle(Update update) {
        log.info("Handling user creation command. {}", update);
        return null;
    }
}
