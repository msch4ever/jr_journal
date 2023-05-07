package cz.los.jr_journal.bot.handler;

import cz.los.jr_journal.bot.BotResponse;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
public class StartHandler extends AbstractCommandHandler implements CommandHandler<SendMessage> {

    private static final String GREETINGS = """
            Привет. Я JavaRush бот, который поможет тебе вести учет проведенных занятий.
            Я могу не только помочь "отметиться в системе", но и предоставить отчет по
            прошлым занятиям, удалить неверные записи, напомнить отметиться в день занятия,
            а так же в конце месяца и многое другое (я пока не знаю что).
            Кек, лол, карвалол.
            """;

    @Override
    public BotResponse<SendMessage> handle(Update update) {
        log.info("Handling start command. {}", update);
        return new BotResponse<>(SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text(GREETINGS)
                .build());
    }


}
