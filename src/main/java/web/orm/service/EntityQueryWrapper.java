package web.orm.service;

import java.util.List;
import java.util.Set;

public interface EntityQueryWrapper {
    List<Object> query(Class<?> entityClass, Set<String> required, String condition, Object...args);
}
