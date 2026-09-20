package web.orm.service.impl;

import web.orm.EntityMetaData;
import web.orm.SqlGenerate;
import web.orm.annotation.Column;
import web.orm.annotation.Id;
import web.orm.annotation.Table;
import web.orm.service.EntityUpdateWrapper;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

public class EntityUpdateWrapperImpl implements EntityUpdateWrapper {
    @Override
    public void delete(Class<?> entityClass,String condition,Object... args) {
        EntityMetaData end = EntityMetaData.parse(entityClass);
        if(end == null) throw new RuntimeException(entityClass+"字节码缺少@Table注解");
        SqlGenerate sg = new SqlGenerate();
        sg.deleteSql(end,condition,args);
    }

    @Override
    public void update(Object entity,String condition,Object...args) {
        EntityMetaData end = EntityMetaData.parse(entity.getClass());
        if(end == null) throw new RuntimeException(entity+"实体类缺少@Table注解");
        SqlGenerate sg = new SqlGenerate();
        sg.updateSql(end,entity,condition,args);
    }

    @Override
    public void insert(Object entity) {
        EntityMetaData end = EntityMetaData.parse(entity.getClass());
        if(end == null) throw new RuntimeException(entity+"实体类缺少@Table注解");
        SqlGenerate sg = new SqlGenerate();
        sg.insertSql(end,entity);
    }

}
