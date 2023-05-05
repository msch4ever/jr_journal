package cz.los.jr_journal.dal.repository;

import cz.los.jr_journal.dal.Repository;
import cz.los.jr_journal.dal.mapper.GroupMapper;
import cz.los.jr_journal.model.Group;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;

import java.util.Optional;

@Slf4j
public class GroupRepository extends AbstractRepository implements Repository<Group> {

    private final Class<GroupMapper> mapperClass;

    public GroupRepository() {
        this.mapperClass = GroupMapper.class;
    }

    @Override
    public void create(Group group) {
        log.info("Attempting to save {} into DB...", group);
        try (SqlSession session = openSession()) {
            GroupMapper mapper = session.getMapper(mapperClass);
            mapper.insertGroup(group);
            session.commit();
            log.info("Group was successfully saved in the DB with id: {}", group.getGroupId());
        } catch (Exception e) {
            String message = String.format("Could not perform %s operation on %s!", "create", group);
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
            GroupMapper mapper = session.getMapper(mapperClass);
            Optional<Group> group = mapper.findByName(name);
            group.ifPresent(value -> log.info("Group was successfully saved in the DB with id: {}", value.getGroupId()));
            return group;
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
            GroupMapper mapper = session.getMapper(mapperClass);
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
            GroupMapper mapper = session.getMapper(mapperClass);
            return mapper.countByName(name);
        } catch (Exception e) {
            String message = "Could not perform countByName!";
            log.error(message + System.lineSeparator() + e.getMessage());
            throw new RuntimeException(message);
        }
    }
}
