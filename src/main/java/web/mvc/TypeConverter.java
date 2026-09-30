package web.mvc;

import lombok.Getter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;


/**
 * 字符串参数 → 目标类型的转换,目前支持 String/int/long/double
 */
public class TypeConverter {
    @Getter
    public static  List<Object> beans;
    public static Object convert(Class<?> type, String value) {
        if (value == null) {
            return null;
        }
        if (type == String.class) {
            return value;
        }
        if (type == int.class || type == Integer.class) {
            return Integer.parseInt(value.trim());
        }
        if (type == long.class || type == Long.class) {
            return Long.parseLong(value.trim());
        }
        if (type == double.class || type == Double.class) {
            return Double.parseDouble(value.trim());
        }
        return value;   // 其他类型暂不支持,直接返回字符串
    }


    public static void setBeans(List<Object> beans){
        TypeConverter.beans = beans;
    }

}