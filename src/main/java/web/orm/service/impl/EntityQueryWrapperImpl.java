package web.orm.service.impl;

import web.orm.EntityMetaData;
import web.orm.SqlGenerate;
import web.orm.service.EntityQueryWrapper;
import java.util.List;
import java.util.Set;

public class EntityQueryWrapperImpl implements EntityQueryWrapper {
    @Override
    public List<Object> query(Class<?> entityClass, Set<String> required, String condition, Object... args) {
        EntityMetaData end = EntityMetaData.parse(entityClass);
        if(end == null) throw new RuntimeException(entityClass+"字节码缺少@Table注解");
        SqlGenerate sg = new SqlGenerate();
        List<Object> rs = sg.querySql(entityClass, end , required, condition, args);
        return rs;
    }

}
