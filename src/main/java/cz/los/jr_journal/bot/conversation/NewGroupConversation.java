package cz.los.jr_journal.bot.conversation;

import cz.los.jr_journal.model.BotUser;
import cz.los.jr_journal.model.Level;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import static cz.los.jr_journal.bot.command.Command.NEW_GROUP;

@Getter
public class NewGroupConversation extends Conversation {

    private static final long NEW_GROUP_COMMAND_TIMEOUT = 60;
    public static final int NEW_GROUP_STEP_QTY = 3; //4

    private String groupName;
    private Level module;
    private List<BotUser> mentors;

    public NewGroupConversation(long chatId) {
        super(chatId, NEW_GROUP, NEW_GROUP_COMMAND_TIMEOUT, NEW_GROUP_STEP_QTY);
        this.mentors = new ArrayList<>();
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setModule(Level module) {
        this.module = module;
    }

    public void addMentor(BotUser mentor) {
        this.mentors.add(mentor);
    }

}
