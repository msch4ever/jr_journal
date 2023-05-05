package cz.los.jr_journal.bot.conversation;

import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConversationKeeper {

    @Getter
    private final Map<ConversationKey, Conversation> conversations;

    public ConversationKeeper() {
        this.conversations = new ConcurrentHashMap<>();
    }

}
