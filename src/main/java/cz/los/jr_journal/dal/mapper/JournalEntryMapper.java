package cz.los.jr_journal.dal.mapper;

import cz.los.jr_journal.model.JournalEntry;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;

public interface JournalEntryMapper {

    @Insert("""
            INSERT INTO journal_entry (
                entry_user_id,
                lector_user_id,
                other_mentor_participation,
                group_id,
                topic,
                date,
                what_could_be_improved,
                general_comment
            )
            VALUES (
                #{entryUserId},
                #{lectorUserId},
                #{otherMentorParticipation},
                #{groupId},
                #{topic},
                #{date},
                #{whatCouldBeImproved},
                #{generalComment}
            );
            """)
    @Options(useGeneratedKeys = true, keyProperty = "entryUserId")
    void insertJournalEntry(JournalEntry journalEntry);

    //ToDo: no Id returned from insert

}
