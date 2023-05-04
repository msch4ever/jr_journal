package cz.los.jr_journal.dal.repository;

import cz.los.jr_journal.dal.Repository;
import cz.los.jr_journal.dal.mapper.GroupMapper;
import cz.los.jr_journal.model.Group;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;

@Slf4j
public class GroupRepository extends AbstractRepository implements Repository<Group> {

    private Class<GroupMapper> mapperClass;

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

    @Override
    public void update(Group entity) {

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
