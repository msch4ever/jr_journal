package cz.los.jr_journal.bot.handler;

import cz.los.jr_journal.bot.BotResponse;
import cz.los.jr_journal.model.BotUser;
import cz.los.jr_journal.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;


@Slf4j
public class RegisterHandler extends AbstractCommandHandler implements CommandHandler<SendMessage> {

    private static final String USER_CREATED = "Тебя успешно зарегистрировали! Твой юзернейм:[%s]";
    private static final String USER_ALREADY_EXISTS = "Ты уже зарегистрирован.. Нет нужды делать это еще раз.";
    private final UserService userService;

    public RegisterHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public BotResponse<SendMessage> handle(Update update) {
        log.info("Handling user creation command. {}", update);
        Optional<BotUser> user = userService.createUser(update);
        return user.map(botUser -> new BotResponse<>(
                SendMessage.builder()
                        .chatId(update.getMessage().getChatId())
                        .text(String.format(USER_CREATED, botUser.getUsername()))
                        .build()))
                .orElseGet(() -> new BotResponse<>(SendMessage.builder()
                        .chatId(update.getMessage().getChatId())
                        .text(USER_ALREADY_EXISTS)
                        .build()));
    }
}
