package cz.los.jr_journal.service;

import cz.los.jr_journal.dal.repository.GroupRepository;
import cz.los.jr_journal.dal.repository.UserRepository;
import cz.los.jr_journal.model.BotUser;
import cz.los.jr_journal.model.Group;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;
import java.util.Optional;

@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    public UserService(UserRepository userRepository, GroupRepository groupRepository) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
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
        userRepository.create(newUser);
        log.info("User with id:{} was created successfully", newUser.getUserId());
        return Optional.of(newUser);
    }

    public void enrichWithGroups(BotUser user) {
        log.info("Enriching user with assigned groups...");
        List<Group> groups = groupRepository.fetchGroupsByUserId(user.getUserId());
        if (!groups.isEmpty()) {
            user.setGroups(groups);
            log.info("User enriched with {} groups.", groups.size());
        } else {
            log.info("No associated groups with userId:{}", user.getUserId());
        }
    }

    private boolean userExists(String username, Long telegramUserId) {
        int existingCount = userRepository.countByUsernameAndTelegramId(username, telegramUserId);
        return existingCount != 0;
    }

    public Optional<BotUser> findUserByTelegramId(long id) {
        log.info("Fetching user by telegramId:{}...", id);
        return userRepository.findByTelegramId(id);
    }

    public Optional<BotUser> findByUsername(String username) {
        log.info("Fetching user by username:{}...", username);
        return userRepository.findByUsername(username);
    }

}
