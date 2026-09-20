package web.orm;

import lombok.Data;
import web.orm.annotation.Column;
import web.orm.annotation.Id;
import web.orm.annotation.Table;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
@Data
public class EntityMetaData {
    private String tableName;
    private Map<Field,String> columns;
    private Field id;
    public EntityMetaData() {
    }
    public EntityMetaData(String tableName, Map<Field, String> columns, Field id) {
        this.tableName = tableName;
        this.columns = columns;
        this.id = id;
    }

    public static EntityMetaData parse(Class<?> clazz) {
        if(clazz.isAnnotationPresent(Table.class)){
            Map<Field,String> columns=new LinkedHashMap<>();
            Field id=null;
            Table table = clazz.getAnnotation(Table.class);
            String tableName = table.value();
            for(Field field: clazz.getDeclaredFields()){
                if(field.isAnnotationPresent(Column.class)){
                    columns.put(field,field.getAnnotation(Column.class).value().isBlank()? field.getName():field.getAnnotation(Column.class).value());
                }else if(field.isAnnotationPresent(Id.class)){
                    id=field;
                    columns.put(field,field.getAnnotation(Id.class).value().isBlank()? field.getName():field.getAnnotation(Id.class).value());
                }else{
                    columns.put(field,field.getName());
                }
            }
            return new EntityMetaData(tableName,columns,id);
        }
        return null;
    }
}
