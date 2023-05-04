package cz.los.jr_journal.dal.repository;

import lombok.SneakyThrows;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.Reader;

import static cz.los.jr_journal.dal.Repository.MYBATIS_CONFIG;

public abstract class AbstractRepository {

    private final SqlSessionFactory sessionFactory;

    @SneakyThrows
    public AbstractRepository() {
        Reader reader = Resources.getResourceAsReader(MYBATIS_CONFIG);
        this.sessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    protected SqlSession openSession() {
        return sessionFactory.openSession();
    }
}
