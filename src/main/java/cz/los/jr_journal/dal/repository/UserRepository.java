package cz.los.jr_journal.dal.repository;

import cz.los.jr_journal.dal.Repository;
import cz.los.jr_journal.dal.mapper.UserMapper;
import cz.los.jr_journal.model.BotUser;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.Reader;

@Slf4j
public class UserRepository implements Repository<BotUser> {

    private final Class<UserMapper> mapperClass;
    private SqlSessionFactory sessionFactory;

    @SneakyThrows
    public UserRepository() {
        this.mapperClass = UserMapper.class;
        Reader reader = Resources.getResourceAsReader(MYBATIS_CONFIG);
        this.sessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    @Override
    public void create(BotUser user) {
        log.info("Attempting to save {} into DB...", user);
        try (SqlSession session = sessionFactory.openSession()) {
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
        try (SqlSession session = sessionFactory.openSession()) {
            UserMapper mapper = session.getMapper(mapperClass);
            return mapper.countByUsernameAndTelegramId(username, telegramUserId);
        } catch (Exception e) {
            String message = "Could not perform countByUsernameAndTelegramId!";
            log.error(message + System.lineSeparator() + e.getMessage());
            throw new RuntimeException(message);
        }
    }
}
