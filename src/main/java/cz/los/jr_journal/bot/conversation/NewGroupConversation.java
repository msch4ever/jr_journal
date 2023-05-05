package cz.los.jr_journal.bot.conversation;

import cz.los.jr_journal.bot.command.Command;
import cz.los.jr_journal.model.BotUser;
import cz.los.jr_journal.model.Level;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class NewGroupConversation extends Conversation {

    private String groupName;
    private Level module;
    private List<BotUser> mentors;

    public NewGroupConversation(long chatId, Command command, long timeout, int stepQty) {
        super(chatId, command, timeout, stepQty);
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
