package cz.los.jr_journal.service;

import cz.los.jr_journal.bot.conversation.AssignMentorConversation;
import cz.los.jr_journal.bot.conversation.NewGroupConversation;
import cz.los.jr_journal.dal.repository.GroupRepository;
import cz.los.jr_journal.model.BotUser;
import cz.los.jr_journal.model.Group;
import cz.los.jr_journal.model.Level;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class GroupService {

    private final GroupRepository repository;

    public GroupService(GroupRepository repository) {
        this.repository = repository;
    }

    public Optional<Group> createGroup(String groupName) {
        log.info("Creating a group...");
        if (groupExists(groupName)) {
            log.warn("Group with name:{} already exists", groupName);
            return Optional.empty();
        }
        Group newGroup = new Group(groupName);
        repository.create(newGroup);
        log.info("Group with id:{} was created successfully", newGroup.getGroupId());
        return Optional.of(newGroup);
    }

    public Optional<Group> createGroup(NewGroupConversation conversation) {
        log.info("Creating a group...");
        if (groupExists(conversation.getGroupName())) {
            log.warn("Group with name:{} already exists", conversation.getGroupName());
            return Optional.empty();
        }
        Group newGroup = new Group(conversation.getGroupName(), conversation.getModule());
        repository.create(newGroup);
        BotUser mentor = conversation.getMentor();
        if (mentor != null) {
            repository.createGroupMentorAssociation(newGroup.getGroupId(), mentor.getUserId());
        }
        log.info("Group with id:{} was created successfully", newGroup.getGroupId());
        return Optional.of(newGroup);
    }

    public Optional<Group> findByName(String name) {
        log.info("Trying to find group by name:{}", name);
        return repository.findByName(name);
    }

    public boolean groupExists(String name) {
        int existingCount = repository.countByName(name);
        return existingCount != 0;
    }

    public Optional<Level> setLevel(String groupName, int level) {
        log.info("Setting a level to the group...");
        Optional<Group> groupOptional = repository.findByName(groupName);
        if (groupOptional.isEmpty()) {
            log.warn("Group with name:{} does not exists", groupName);
            return Optional.empty();
        }
        Group group = groupOptional.get();
        Level module = Level.getByNumber(level).orElseThrow(() -> new IllegalArgumentException("Could not resolve level!"));
        group.setModule(module);
        repository.update(group);
        log.info("Group with id:{} was assigned with level {}", group.getGroupId(), module.name());
        return Optional.of(module);
    }

    public void assignMentor(AssignMentorConversation conversation) {
        BotUser mentor = conversation.getMentor();
        Group group = conversation.getGroup();
        repository.createGroupMentorAssociation(group.getGroupId(), mentor.getUserId());
        log.info("Mentor:{} was assigned to group:{} successfully", mentor.getUsername(), group.getDisplayName());
    }
}
