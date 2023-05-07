package cz.los.jr_journal.bot.conversation;

import lombok.Getter;
import lombok.Setter;

import java.time.Month;
import java.time.MonthDay;
import java.time.Year;

import static cz.los.jr_journal.bot.command.Command.REPORT;

@Getter
@Setter
public class ReportConversation extends Conversation {

    private static final int REPORT_STEP_QTY = 8;
    private static final int REPORT_STEP_TIMEOUT = 20;

    public ReportConversation(long chatId) {
        super(chatId, REPORT, REPORT_STEP_TIMEOUT, REPORT_STEP_QTY);
    }

    private Year year;
    private Month month;
    private MonthDay day;


}
