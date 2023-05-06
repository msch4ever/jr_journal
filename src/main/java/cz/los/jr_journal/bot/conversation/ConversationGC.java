package cz.los.jr_journal.bot.conversation;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ConversationGC {

    private static final int GC_INTERVAL = 10;
    private final ConversationKeeper conversationKeeper;
    private final ScheduledExecutorService gc;
    public ConversationGC(ConversationKeeper conversationKeeper) {
        this.conversationKeeper = conversationKeeper;
        this.gc = Executors.newSingleThreadScheduledExecutor();
    }

    public void startGC() {
        gc.scheduleAtFixedRate(new GcTask(conversationKeeper), 0, GC_INTERVAL, TimeUnit.SECONDS);
    }

    private static class GcTask extends Thread {

        private static final String GC_THREAD = "GC-thread";

        private final ConversationKeeper conversationKeeper;
        public GcTask(ConversationKeeper conversationKeeper) {
            this.conversationKeeper = conversationKeeper;
            setName(GC_THREAD);
        }

        @Override
        public void run() {
            log.info("Starting conversations GC...");
            Map<Long, Conversation> conversationMap = conversationKeeper.getConversations();
            if (conversationMap.isEmpty()) {
                log.info("Conversation map is empty. Nothing to do here...");
                return;
            }
            List<Long> toBeRemoved = new ArrayList<>();
            conversationMap.forEach((key, value) -> {
                long now = System.currentTimeMillis();
                long timeout = value.getTimeout();
                long lastInteracted = value.getLastInteracted();
                if (now - lastInteracted > timeout * 1000) {
                    toBeRemoved.add(key);
                }
            });
            for (Long key : toBeRemoved) {
                conversationMap.remove(key);
            }
            log.info("GC finished. Removed {} conversations!", toBeRemoved.size());
        }
    }
}
