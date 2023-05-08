package cz.los.jr_journal.bot.conversation;

import cz.los.jr_journal.bot.command.Command;
import cz.los.jr_journal.model.BotUser;
import lombok.Getter;
import lombok.Setter;

@Getter
public abstract class Conversation {

    protected final long chatId;
    protected final Command command;
    protected final long timeout;
    protected final int stepQty;
    protected long lastInteracted;
    @Setter
    protected int step;
    @Setter
    protected BotUser botUser;

    public Conversation(long chatId, Command command, long timeout, int stepQty) {
        this.chatId = chatId;
        this.command = command;
        this.timeout = timeout;
        this.lastInteracted = System.currentTimeMillis();
        this.stepQty = stepQty;
        this.step = 0;
    }

    public void incrementStep() {
        if (step <= stepQty) {
            lastInteracted = System.currentTimeMillis();
            step++;
        } else {
            throw new IllegalStateException("Could not increment step over available qty!");
        }
    }
}
