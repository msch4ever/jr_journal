package cz.los.jr_journal.bot.conversation;

import cz.los.jr_journal.model.Attendance;
import cz.los.jr_journal.model.Group;
import cz.los.jr_journal.model.JournalEntry;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.time.Year;

import static cz.los.jr_journal.bot.command.Command.REPORT;

@Getter
@Setter
@ToString
public class ReportConversation extends Conversation {

    private static final int REPORT_STEP_QTY = 12;
    private static final int REPORT_STEP_TIMEOUT = 20;

    public ReportConversation(long chatId) {
        super(chatId, REPORT, REPORT_STEP_TIMEOUT, REPORT_STEP_QTY);
    }

    private ReportConversationStep reportStep;
    private Year year;
    private Month month;
    private MonthDay day;
    private Group group;
    private boolean userConductedClass;
    private String topic;
    private Attendance otherMentorAttendance;
    private String whatCouldBeImproved;
    private String additionalComments;

    private JournalEntry journalEntry;

    public String getSummary() {
        return String.format("""
                        Занятие прошло %s
                        В Группе %s
                        %s
                        Дургой/ая метор/киня присутствовал/ла на занятии: %s
                        Можно ли что-то улучшить? - %s
                        Дополнительные комментарии - %s
                        """,
                LocalDate.of(year.getValue(), month, day.getDayOfMonth()),
                group.getDisplayName(),
                userConductedClass ? "Занятие было проведено тобой" : "Другой/ая ментор/киня проводил/ла занятие",
                otherMentorAttendance,
                whatCouldBeImproved != null ? whatCouldBeImproved : "нет комментария",
                additionalComments != null ? additionalComments : "нет комментария");
    }

    public void setReportStep(ReportConversationStep reportStep) {
        this.lastInteracted = System.currentTimeMillis();
        this.reportStep = reportStep;
    }

    public enum ReportConversationStep {
        YEAR, MONTH, DAY, GROUP, TOPIC, WHO, OTHER_MENTOR,
        WHAT_COULD_BE_IMPROVED_DECISION, WHAT_COULD_BE_IMPROVED_COMMENT,
        COMMENT_DECISION, COMMENT, CONFIRMATION
    }

}
