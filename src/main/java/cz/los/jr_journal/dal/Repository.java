package cz.los.jr_journal.dal;

public interface Repository<T> {

    String MYBATIS_CONFIG = "mybatis-config.xml";
    void create(T entity);
    T read(Long id);
    void update(T entity);
    void delete(Long id);

}
