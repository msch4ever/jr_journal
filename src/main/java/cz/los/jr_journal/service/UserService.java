package cz.los.jr_journal.service;

import cz.los.jr_journal.dal.repository.UserRepository;
import cz.los.jr_journal.model.BotUser;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Optional;

@Slf4j
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public Optional<BotUser> createUser(Message message) {
        log.info("Creating a user...");
        User from = message.getFrom();
        Long telegramUserId = from.getId();
        String username = from.getUserName();
        String firstName = from.getFirstName();
        String lastName = from.getLastName();
        if (userExists(username, telegramUserId)) {
            log.warn("User with telegramUserId:{} and username:{} already exists", telegramUserId, username);
            return Optional.empty();
        }
        BotUser newUser = new BotUser(telegramUserId, username, firstName, lastName);
        repository.create(newUser);
        log.info("User with id:{} was created successfully", newUser.getUserId());
        return Optional.of(newUser);
    }

    private boolean userExists(String username, Long telegramUserId) {
        int existingCount = repository.countByUsernameAndTelegramId(username, telegramUserId);
        return existingCount != 0;
    }

    public Optional<BotUser> findUserByTelegramId(long id) {
        log.info("Fetching user by telegramId:{}...", id);
        return repository.findByTelegramId(id);
    }

    public Optional<BotUser> findByUsername(String username) {
        log.info("Fetching user by username:{}...", username);
        return repository.findByUsername(username);
    }

}
