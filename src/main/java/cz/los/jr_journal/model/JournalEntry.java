package cz.los.jr_journal.model;

import java.time.Duration;
import java.time.LocalDate;

public class JournalEntry {

    private Long entryId;
    private Long entryUserId;
    private Long lectorUserId;
    private Attendance otherMentorParticipation;
    private Long groupId;
    private String topic;
    private LocalDate date;
    private Duration duration;
    private String whatCouldBeImproved;
    private String generalComment;

}
