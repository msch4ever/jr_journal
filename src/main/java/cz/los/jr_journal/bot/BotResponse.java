package cz.los.jr_journal.bot;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

import java.util.Objects;

public class BotResponse<Method extends BotApiMethod> {

    private final Method response;

    public BotResponse(Method response) {
        this.response = response;
    }

    public boolean isPresent() {
        return Objects.nonNull(response);
    }
    public BotApiMethod getResponse() {
        return response;
    }
}
