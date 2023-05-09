package cz.los.jr_journal.service;

import cz.los.jr_journal.bot.conversation.ReportConversation;
import cz.los.jr_journal.dal.repository.JournalEntryRepository;
import cz.los.jr_journal.model.JournalEntry;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class JournalEntryService {

    private final JournalEntryRepository repository;

    public JournalEntryService(JournalEntryRepository repository) {
        this.repository = repository;
    }

    public Optional<JournalEntry> createEntry(ReportConversation conversation) {
        log.info("Creating a journal entry...");
        JournalEntry newEntry = new JournalEntry(conversation);
        repository.create(newEntry);
        log.info("Entry with id:{} was created successfully", newEntry.getEntryId());
        return Optional.of(newEntry);
    }

}
