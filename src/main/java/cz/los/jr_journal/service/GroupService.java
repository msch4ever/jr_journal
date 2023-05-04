package cz.los.jr_journal.service;

import cz.los.jr_journal.dal.repository.GroupRepository;
import cz.los.jr_journal.model.Group;
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
        Group newGroup = Group.builder().name(groupName).build();
        repository.create(newGroup);
        log.info("Group with id:{} was created successfully", newGroup.getGroupId());
        return Optional.of(newGroup);
    }

    private boolean groupExists(String name) {
        int existingCount = repository.countByName(name);
        return existingCount != 0;
    }

}
