package web.orm.service;

public interface EntityUpdateWrapper {
    void insert( Object entity) ;
    void update( Object entity,String condition,Object...args) ;
    void delete( Class<?> entityClass,String condition,Object... args) ;
}
