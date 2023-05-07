package cz.los.jr_journal.bot.conversation;

import cz.los.jr_journal.model.BotUser;
import cz.los.jr_journal.model.Group;
import lombok.Getter;
import lombok.Setter;

import static cz.los.jr_journal.bot.command.Command.ASSIGN_MENTOR;

@Getter
@Setter
public class AssignMentorConversation extends Conversation {

    private static final long ASSIGN_MENTOR_COMMAND_TIMEOUT = 60;
    public static final int ASSIGN_MENTOR_STEP_QTY = 4;

    private BotUser mentor;
    private Group group;

    public AssignMentorConversation(long chatId) {
        super(chatId, ASSIGN_MENTOR, ASSIGN_MENTOR_COMMAND_TIMEOUT, ASSIGN_MENTOR_STEP_QTY);
    }
}
