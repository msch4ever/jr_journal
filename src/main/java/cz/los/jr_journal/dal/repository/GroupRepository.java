package cz.los.jr_journal.dal.repository;

import cz.los.jr_journal.dal.Repository;
import cz.los.jr_journal.dal.mapper.GroupMapper;
import cz.los.jr_journal.dal.mapper.GroupMentorMapper;
import cz.los.jr_journal.model.Group;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
public class GroupRepository extends AbstractRepository implements Repository<Group> {

    private final Class<GroupMapper> groupMapperClass;
    private final Class<GroupMentorMapper> groupMentorMapperClass;

    public GroupRepository() {
        this.groupMapperClass = GroupMapper.class;
        this.groupMentorMapperClass = GroupMentorMapper.class;
    }

    @Override
    public void create(Group group) {
        log.info("Attempting to save {} into DB...", group);
        try (SqlSession session = openSession()) {
            GroupMapper mapper = session.getMapper(groupMapperClass);
            mapper.insertGroup(group);
            session.commit();
            log.info("Group was successfully saved in the DB with id: {}", group.getGroupId());
        } catch (Exception e) {
            String message = String.format("Could not perform %s operation on %s!", "create", group);
            log.error(message + System.lineSeparator() + e.getMessage());
            throw new RuntimeException(message);
        }
    }

    public void createGroupMentorAssociation(long groupId, long mentorId) {
        log.info("Attempting to create association for groupId:{} and mentorId:{}...", groupId, mentorId);
        try (SqlSession session = openSession()) {
            GroupMentorMapper mapper = session.getMapper(groupMentorMapperClass);
            mapper.createAssociation(groupId, mentorId);
            session.commit();
            log.info("Group was successfully assigned to mentor");
        } catch (Exception e) {
            String message = String.format("Could not perform %s operationa", "createGroupMentorAssociation");
            log.error(message + System.lineSeparator() + e.getMessage());
            throw new RuntimeException(message);
        }
    }

    public List<Group> fetchGroupsByUserId(long userId) {
        try (SqlSession session = openSession()) {
            GroupMentorMapper groupMentorMapper = session.getMapper(groupMentorMapperClass);
            List<Long> groupIds = groupMentorMapper.getGroupIdsByUserId(userId);
            if (groupIds.isEmpty()) {
                log.info("No Groups associated with userId:{}", userId);
                return Collections.emptyList();
            }
            GroupMapper groupMapper = session.getMapper(groupMapperClass);
            List<Group> groups = groupMapper.getByIdInList(groupIds);
            log.info("{} groups fetched for userId:{}", groups.size(), userId);
            return groups;
        } catch (Exception e) {
            String message = "Could not perform fetchGroupsByUserId!";
            log.error(message + System.lineSeparator() + e.getMessage());
            throw new RuntimeException(message);
        }
    }

    @Override
    public Group read(Long id) {
        return null;
    }

    public Optional<Group> findByName(String name) {
        log.info("Attempting to find group by name {}...", name);
        try (SqlSession session = openSession()) {
            GroupMapper mapper = session.getMapper(groupMapperClass);
            return mapper.findByName(name.toLowerCase());
        } catch (Exception e) {
            String message = String.format("Could not perform %s operation on %s!", "findByName", name);
            log.error(message + System.lineSeparator() + e.getMessage());
            throw new RuntimeException(message);
        }
    }

    @Override
    public void update(Group group) {
        log.info("Attempting to update {} in the DB...", group);
        try (SqlSession session = openSession()) {
            GroupMapper mapper = session.getMapper(groupMapperClass);
            mapper.updateGroup(group);
            session.commit();
            log.info("Group with id: {} was successfully updated in the DB!", group.getGroupId());
        } catch (Exception e) {
            String message = String.format("Could not perform %s operation on %s!", "update", group);
            log.error(message + System.lineSeparator() + e.getMessage());
            throw new RuntimeException(message);
        }
    }

    @Override
    public void delete(Long id) {

    }

    public int countByName(String name) {
        try (SqlSession session = openSession()) {
            GroupMapper mapper = session.getMapper(groupMapperClass);
            return mapper.countByName(name.toLowerCase());
        } catch (Exception e) {
            String message = "Could not perform countByName!";
            log.error(message + System.lineSeparator() + e.getMessage());
            throw new RuntimeException(message);
        }
    }
}
