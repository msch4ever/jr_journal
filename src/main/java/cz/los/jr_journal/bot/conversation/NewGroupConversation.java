package cz.los.jr_journal.bot.conversation;

import cz.los.jr_journal.model.BotUser;
import cz.los.jr_journal.model.Level;
import lombok.Getter;

import static cz.los.jr_journal.bot.command.Command.NEW_GROUP;

@Getter
public class NewGroupConversation extends Conversation {

    private static final long NEW_GROUP_COMMAND_TIMEOUT = 60;
    public static final int NEW_GROUP_STEP_QTY = 4;

    private String groupName;
    private Level module;
    private BotUser mentor;

    public NewGroupConversation(long chatId) {
        super(chatId, NEW_GROUP, NEW_GROUP_COMMAND_TIMEOUT, NEW_GROUP_STEP_QTY);
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName.trim();
    }

    public void setModule(Level module) {
        this.module = module;
    }

    public void setMentor(BotUser mentor) {
        this.mentor = mentor;
    }

}
