package org.example.jdbc;

import org.example.exception.DataAccessException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

// 解析jdbc的结果，装入T类型的bean
public class BeanRowMapper<T> implements RowMapper<T>{
    Class<T> clazz;
    Constructor<T> constructor;
    Map<String, Field> fields = new HashMap<>();
    Map<String, Method> methods = new HashMap<>();

    public BeanRowMapper(Class<T> clazz) {
        this.clazz = clazz;
        try{
            this.constructor = this.clazz.getConstructor();
        }catch (ReflectiveOperationException e){
            throw new DataAccessException(String.format("can not found public default constructor in %s",this.clazz.getName()));
        }
        // 读取fields和methods
        for (Field f : clazz.getFields()){
            String name = f.getName();
            this.fields.put(name, f);
        }
        // getMethods只返回public修饰的方法
        for (Method m: clazz.getMethods()){
            Parameter[] ps = m.getParameters();
            if(ps.length == 1){
                String name = m.getName();
                // 晒出setter方法
                if(name.length() >= 4 && name.startsWith("set")){
                    String prop = Character.toLowerCase(name.charAt(3)) + name.substring(4);
                    this.methods.put(prop,m);
                }
            }
        }
    }

    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        T bean;
        try{
            bean = this.constructor.newInstance();
            ResultSetMetaData meta = rs.getMetaData();
            int columns = meta.getColumnCount();
            for(int i = 1; i <= columns; i++){
                String label = meta.getColumnLabel(i);
                Method method = this.methods.get(label);
                if (method != null){
                    method.invoke(bean, rs.getObject(label));
                }else {
                    Field field = this.fields.get(label);
                    if (field != null){
                        field.set(bean, rs.getObject(label));
                    }
                }
            }
        }catch (ReflectiveOperationException e){
            throw new DataAccessException(String.format("could not map result to class s%", this.clazz.getName()));
        }
        return bean;
    }
}
