package cz.los.jr_journal.dal.repository;

import cz.los.jr_journal.dal.Repository;
import cz.los.jr_journal.dal.mapper.GroupMapper;
import cz.los.jr_journal.dal.mapper.JournalEntryMapper;
import cz.los.jr_journal.model.Group;
import cz.los.jr_journal.model.JournalEntry;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;

@Slf4j
public class JournalEntryRepository extends AbstractRepository implements Repository<JournalEntry> {

    private final Class<JournalEntryMapper> journalEntryMapperClass;

    public JournalEntryRepository() {
        this.journalEntryMapperClass = JournalEntryMapper.class;
    }

    @Override
    public void create(JournalEntry journalEntry) {
        log.info("Attempting to save {} into DB...", journalEntry);
        try (SqlSession session = openSession()) {
            JournalEntryMapper mapper = session.getMapper(journalEntryMapperClass);
            mapper.insertJournalEntry(journalEntry);
            session.commit();
            log.info("JournalEntry was successfully saved in the DB with id: {}", journalEntry.getEntryId());
        } catch (Exception e) {
            String message = String.format("Could not perform %s operation on %s!", "create", journalEntry);
            log.error(message + System.lineSeparator() + e.getMessage());
            throw new RuntimeException(message);
        }
    }

    @Override
    public JournalEntry read(Long id) {
        return null;
    }

    @Override
    public void update(JournalEntry entity) {

    }

    @Override
    public void delete(Long id) {

    }
}
