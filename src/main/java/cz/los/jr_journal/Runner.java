package cz.los.jr_journal;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
public class Runner {

    public static void main(String[] args) {
        try {
            new App().run(args);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
