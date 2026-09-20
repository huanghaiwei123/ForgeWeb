package web.orm;

import lombok.val;
import web.orm.annotation.Column;
import web.orm.annotation.Id;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

public class SqlGenerate {
    /**
     * sql插入语句
     * @param end
     * @return
     */
    public void insertSql(EntityMetaData end,Object entity){
        String tableName = end.getTableName();
        Field id = end.getId();
        Map<Field, String> columns = end.getColumns();
        Map<String, String> infos = getJdbcInformation();
        String driver = infos.get("driver");
        String url = infos.get("url");
        String user = infos.get("user");
        String password = infos.get("password");
        Connection conn;
        conn = null;
        PreparedStatement statement = null;
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url,user,password);
            //4、执行sql
            String sql = "insert into " + tableName + "(";
            List<Field> fields = columns.keySet().stream().toList();
            List<String> names = columns.values().stream().toList();
            for(int i=0;i<names.size();i++){
                if(i<names.size()-1){
                    sql+=names.get(i)+",";
                }else {
                    sql += names.get(i);
                }
            }
            sql+=")values(";
           for(int i=0;i<fields.size();i++){
              if(i<fields.size()-1){
                  sql+= "?,";
              }else {
                  sql += "?";
              }
           }
           sql+=")";
            //专门执行DML语句（insert、delete、update）
            //返回值是“影响数据库中的记录条数”
             statement=conn.prepareStatement(sql);
            int index = 1;
            for (Field field : fields) {
                field.setAccessible(true);
                statement.setObject(index++, field.get(entity));
            }
            int a = statement.executeUpdate();
            System.out.println(a);
            System.out.println(a == 1 ? "保存成功":"保存失败");

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException | IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            //6、释放资源
            //为了保证资源一定释放，在finally语句块中关闭资源
            //分别要遵循从小到大依次关闭
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
    }

    /**
     * sql查询语句，根据条件查询想要的字段，这里要求封装类字段必须为引用数据类型
     *
     * @param args,数据库实体字段名称,可以为“*”
     * @param condition,可以为null
     * @param end
     * @param entityClass,类对象
     */
    public List<Object> querySql(Class<?> entityClass, EntityMetaData end, Set<String> required,String condition, Object ...args){
        Map<String, String> infos = getJdbcInformation();
        String driver = infos.get("driver");
        String url = infos.get("url");
        String user = infos.get("user");
        String password = infos.get("password");
        Connection conn = null;
        List<String> fields = required.stream().toList();
        boolean flag=false;
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, user, password);
            if(fields.contains("*")){
                flag=true;
            }
            if(flag){
                 return queryWithAll(entityClass, conn, end, condition,args);
            }else {
                 return queryNotAll(entityClass,conn, end, fields,condition, args);
            }
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }

    }

    /**
     * sql更新语句,只用来根据主键更新单条数据,预编译可以解决字符串和sql注入问题
     *
     * @param end
     * @param entity
     * @param condition
     */
    public void updateSql(EntityMetaData end, Object entity, String condition,Object...args) {
        String tableName = end.getTableName();
        Map<Field, String> columns = end.getColumns();
        Map<String, String> infos = getJdbcInformation();
        String driver = infos.get("driver");
        String url = infos.get("url");
        String user = infos.get("user");
        String password = infos.get("password");
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            Class.forName(driver);
            conn=DriverManager.getConnection(url,user,password);
            String sql = "update " + tableName + " set ";
            List<Field> fields = columns.keySet().stream().toList();
            List<Field> fieldList = new LinkedList();
            int turn=0;
            for(int i=0;i<fields.size();i++){
                Field field = fields.get(i);
                String name = null;
                field.setAccessible(true);
                if(field.get(entity)!=null){
                    if(field.isAnnotationPresent(Id.class)){
                        name = field.getAnnotation(Id.class).value().isBlank()?field.getName():field.getAnnotation(Id.class).value();
                    } else if (field.isAnnotationPresent(Column.class)) {
                        name = field.getAnnotation(Column.class).value().isBlank()?field.getName():field.getAnnotation(Column.class).value();
                    }else{
                        name=field.getName();
                    }
                    if(turn == 0){
                        turn+=1;
                        sql+=name+"=?";
                    }else{
                        sql+=","+name+"=?";
                    }
                    fieldList.add(field);
                }
            }
            if(!fieldList.isEmpty()){
                sql+=" where "+condition+";";
                stmt=conn.prepareStatement(sql);
                for(int i=1;i<=fieldList.size();i++){
                    stmt.setObject(i,fieldList.get(i-1).get(entity));
                }
                for(int i=fieldList.size()+1;i<=fieldList.size()+ args.length;i++){
                    stmt.setObject(i,args[i-fieldList.size()-1]);
                }
            }
            int update = stmt.executeUpdate();
            System.out.println(update==1?"更新成功":"更新失败");
        } catch (SQLException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally{
            try {
                conn.close();
                stmt.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * sql删除语句，根据条件删除和类对象来删除对应 数据
     * @param end
     * @param condition
     */
    public void deleteSql(EntityMetaData end, String condition,Object...args) {
        String tableName = end.getTableName();
        Field id = end.getId();
        id.setAccessible(true);
        Map<Field, String> columns = end.getColumns();
        Map<String, String> infos = getJdbcInformation();
        String driver = infos.get("driver");
        String url = infos.get("url");
        String user = infos.get("user");
        String password = infos.get("password");
        String sql = null;
        if(condition!=null &&!condition.isBlank()){
            sql = "delete from " + tableName + " where "+condition+";";
        }else{
            sql = "delete from " + tableName;
        }
        try( Connection conn=DriverManager.getConnection(url,user,password);
             PreparedStatement stmt=conn.prepareStatement(sql)) {
             Class.forName(driver);
             for(int i=1;i<=args.length;i++){
                 stmt.setObject(i,args[i-1]);
             }
             int delete = stmt.executeUpdate();
             System.out.println(delete==1?"删除成功后":"删除失败");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 查询全部字段，条件可以为null或“”，返回一个映射好的字段
      * @param entityClass
     * @param conn
     * @param end
     * @param condition
     * @return
     */
    private List<Object> queryWithAll(Class<?> entityClass, Connection conn, EntityMetaData end, String condition,Object...args) {
        String tableName = end.getTableName();
        Map<Field, String> columns = end.getColumns();
        List<Field> fields = new LinkedList<>(columns.keySet());
        String sql = "select * from "+tableName;
        Object instance = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Object> entities = new ArrayList<>();
        if(condition!=null&&!condition.isBlank()){
            sql+=" where "+condition;
        }
        sql+=";";
        try {
             stmt = conn.prepareStatement(sql);
            for(int i=1;i<=args.length;i++){
                stmt.setObject(i,args[i-1]);
            }
             rs = stmt.executeQuery();
            while(rs.next()){
                instance=entityClass.getConstructor().newInstance();
                for(Field field:fields){
                    field.setAccessible(true);
                    Object result = rs.getObject(columns.get(field));
                    if(result!=null){
                    field.set(instance, web.orm.TypeConverter.convert(field.getType(),result.toString()));
                   }else{
                        field.set(instance,null);
                    }
                }

                entities.add(instance);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }finally{
            try {
                stmt.close();
                rs.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return entities;
    }

    /**
     * 查询制定字段，条件可以我null或“”，返回一个映射好的字段，没有指定的字段映射为null
     * @param entityClass
     * @param conn
     * @param end
     * @param condition
     * @param args
     * @return
     */
    private List<Object> queryNotAll(Class<?> entityClass, Connection conn,  EntityMetaData end, List<String> required,String condition,Object... args) {
        String tableName = end.getTableName();
        Map<Field, String> columns = end.getColumns();
        Set<Field> fields = columns.keySet();
        Object instance = null;
        String sql = "select " ;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Object> entities = new ArrayList<>();
        for(int i=0;i<required.size();i++){
            if(i<required.size()-1){
                sql+=required.get(i)+",";
            }else{
                sql+=required.get(i);
            }
        }
        sql+=" from "+tableName;
        if(condition!=null&&!condition.isBlank()){
            sql+=" where "+condition;
        }
        sql+=";";
        try {
             stmt = conn.prepareStatement(sql);
             for(int i=1;i<=args.length;i++){
                 stmt.setObject(i,args[i-1]);
             }
             rs = stmt.executeQuery();
            while(rs.next()){
                instance=entityClass.getConstructor().newInstance();
               for(Field field:fields){
                   String name;
                   String result;
                   field.setAccessible(true);
                   if(required.contains(columns.get(field))){
                       if(field.isAnnotationPresent(Id.class)){
                           name=field.getAnnotation(Id.class).value().isBlank()?field.getName():field.getAnnotation(Id.class).value();
                           result=rs.getObject(name)==null?null:rs.getObject(name).toString();
                       }else if(field.isAnnotationPresent(Column.class)){
                           name=field.getAnnotation(Column.class).value().isBlank()?field.getName():field.getAnnotation(Column.class).value();
                           result=rs.getObject(name)==null?null:rs.getObject(name).toString();
                       }else{
                           name=field.getName();
                           result=rs.getObject(name)==null?null:rs.getObject(name).toString();
                       }
                       field.set(instance,TypeConverter.convert(field.getType(),result));
                   }else{
                       field.set(instance,null);
                   }
               }
                entities.add(instance);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }finally{
            try {
                stmt.close();
                rs.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return entities;
    }

    /**
     * 返回jdbc相关配置信息
     * @return
     */
    public Map<String,String> getJdbcInformation(){
        Map<String,String> map = new HashMap<>();
        ResourceBundle bundle = ResourceBundle.getBundle("jdbc");
        String driver = bundle.getString("driver");
        String url = bundle.getString("url");
        String user = bundle.getString("user");
        String password = bundle.getString("password");
        map.put("driver",driver);
        map.put("url",url);
        map.put("user",user);
        map.put("password",password);
        return map;
    }
}
