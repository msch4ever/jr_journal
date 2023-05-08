package cz.los.jr_journal.dal.repository;

import cz.los.jr_journal.dal.Repository;
import cz.los.jr_journal.dal.mapper.GroupMentorMapper;
import cz.los.jr_journal.dal.mapper.UserMapper;
import cz.los.jr_journal.model.BotUser;
import cz.los.jr_journal.model.Group;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.Reader;
import java.util.List;
import java.util.Optional;

@Slf4j
public class UserRepository extends AbstractRepository implements Repository<BotUser> {

    private final Class<UserMapper> mapperClass;
    private final Class<GroupMentorMapper> groupMentorMapperClass;
    public UserRepository() {
        this.mapperClass = UserMapper.class;
        this.groupMentorMapperClass = GroupMentorMapper.class;
    }

    @Override
    public void create(BotUser user) {
        log.info("Attempting to save {} into DB...", user);
        try (SqlSession session = openSession()) {
            UserMapper mapper = session.getMapper(mapperClass);
            mapper.insertUser(user);
            session.commit();
            log.info("User was successfully saved in the DB with id: {}", user.getUserId());
        } catch (Exception e) {
            String message = String.format("Could not perform %s operation on %s!", "create", user);
            log.error(message + System.lineSeparator() + e.getMessage());
            throw new RuntimeException(message);
        }
    }

    @Override
    public BotUser read(Long id) {
        return null;//mapper.getUserById(id);
    }

    @Override
    public void update(BotUser user) {
       // mapper.updateUser(user);
    }

    @Override
    public void delete(Long id) {
        //mapper.deleteUser(id);
    }

    public int countByUsernameAndTelegramId(String username, Long telegramUserId) {
        try (SqlSession session = openSession()) {
            UserMapper mapper = session.getMapper(mapperClass);
            return mapper.countByUsernameAndTelegramId(username, telegramUserId);
        } catch (Exception e) {
            String message = "Could not perform countByUsernameAndTelegramId!";
            log.error(message + System.lineSeparator() + e.getMessage());
            throw new RuntimeException(message);
        }
    }

    public Optional<BotUser> findByTelegramId(long telegramUserId) {
        try (SqlSession session = openSession()) {
            UserMapper mapper = session.getMapper(mapperClass);
            BotUser user = mapper.findByTelegramId(telegramUserId);
            return user != null ? Optional.of(user) :Optional.empty();
        } catch (Exception e) {
            String message = "Could not perform findByTelegramId!";
            log.error(message + System.lineSeparator() + e.getMessage());
            throw new RuntimeException(message);
        }
    }

    public Optional<BotUser> findByUsername(String username) {
        try (SqlSession session = openSession()) {
            UserMapper mapper = session.getMapper(mapperClass);
            BotUser user = mapper.findByUsername(username);
            return user != null ? Optional.of(user) :Optional.empty();
        } catch (Exception e) {
            String message = "Could not perform findByUsername!";
            log.error(message + System.lineSeparator() + e.getMessage());
            throw new RuntimeException(message);
        }
    }
}
