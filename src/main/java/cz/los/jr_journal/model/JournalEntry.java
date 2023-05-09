package cz.los.jr_journal.model;

import cz.los.jr_journal.bot.conversation.ReportConversation;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class JournalEntry {

    private Long entryId;
    private Long entryUserId;
    private Long lectorUserId;
    private Attendance otherMentorParticipation;
    private Long groupId;
    private String topic;
    private LocalDate date;
    private String whatCouldBeImproved;
    private String generalComment;

    public JournalEntry(ReportConversation conversation) {
        this.entryUserId = conversation.getBotUser().getUserId();
        this.lectorUserId = conversation.isUserConductedClass() ? conversation.getBotUser().getUserId() : null;
        this.otherMentorParticipation = conversation.getOtherMentorAttendance();
        this.groupId = conversation.getGroup().getGroupId();
        this.topic = conversation.getTopic();
        this.date = LocalDate.of(conversation.getYear().getValue(), conversation.getMonth(), conversation.getDay().getDayOfMonth());
        this.whatCouldBeImproved = conversation.getWhatCouldBeImproved();
        this.generalComment = conversation.getAdditionalComments();
    }

    public JournalEntry(Long entryId, Long entryUserId, Long lectorUserId, Attendance otherMentorParticipation,
                        Long groupId, String topic, LocalDate date, String whatCouldBeImproved, String generalComment) {
        this.entryId = entryId;
        this.entryUserId = entryUserId;
        this.lectorUserId = lectorUserId;
        this.otherMentorParticipation = otherMentorParticipation;
        this.groupId = groupId;
        this.topic = topic;
        this.date = date;
        this.whatCouldBeImproved = whatCouldBeImproved;
        this.generalComment = generalComment;
    }

}
