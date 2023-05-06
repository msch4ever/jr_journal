package cz.los.jr_journal.bot.conversation;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ConversationKeeper {

    @Getter
    private final Map<Long, Conversation> conversations;

    public ConversationKeeper() {
        this.conversations = new ConcurrentHashMap<>();
    }

    public boolean add(Conversation conversation) {
        long chatId = conversation.getChatId();
        log.info("Adding conversation for chatId:{} command:{}", conversation.getChatId(), conversation.getCommand());
        if (conversations.containsKey(chatId)) {
            log.warn("There is already ongoing conversation");
            return false;
        } else {
            conversations.put(chatId, conversation);
            log.info("Conversation added successfully!");
            return true;
        }
    }

    public boolean conversationExists(long chatId) {
        return conversations.containsKey(chatId);
    }

    public Conversation get(long chatId) {
        return conversations.get(chatId);
    }

    public void remove(long chatId) {
        conversations.remove(chatId);
    }
}
