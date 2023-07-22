package org.example.config;

import com.sun.istack.internal.NotNull;

import java.time.*;
import java.util.*;
import java.util.function.Function;

public class PropertyResolver {
    Map<String, String> properties = new HashMap<>();
    // converters map key=任意类型的字节码对象，value = lambda函数，指定转换方式
    Map<Class<?>,Function<String,Object>> converters = new HashMap<>();

    public PropertyResolver(Properties props) {
        // 环境变量
        this.properties.putAll(System.getenv());
        // stringPropertyNames需要key 和 value 都是 String类型，会出现配置遗漏
        Set<String> names = props.stringPropertyNames();
//        Set<String> names = props.stringPropertyNames();
        for(String name:names){
            this.properties.put(name,props.getProperty(name));
        }
        // register converter
        converters.put(String.class, s->s);

        converters.put(boolean.class, Boolean::parseBoolean);
        converters.put(Boolean.class,Boolean::valueOf);

        converters.put(byte.class,Byte::parseByte);
        converters.put(Byte.class, Byte::valueOf);

        converters.put(short.class,Short::parseShort);
        converters.put(Short.class, Short::valueOf);

        converters.put(int.class, Integer::parseInt);
        converters.put(Integer.class, Integer::valueOf);

        converters.put(long.class, Long::parseLong);
        converters.put(Long.class, Long::valueOf);

        converters.put(float.class, Float::parseFloat);
        converters.put(Float.class, Float::valueOf);

        converters.put(double.class, Double::parseDouble);
        converters.put(Double.class, Double::valueOf);
        // Data type
        converters.put(LocalDate.class, LocalDate::parse);
        converters.put(LocalTime.class, LocalTime::parse);
        converters.put(LocalDateTime.class, LocalDateTime::parse);
        converters.put(ZonedDateTime.class, ZonedDateTime::parse);
        converters.put(Duration.class, Duration::parse);
        converters.put(ZoneId.class, ZoneId::of);

    }
    // key包含判定
    public boolean containsProperty(String key){
        return this.properties.containsKey(key);
    }
    // 查询
    @NotNull
    public String getProperty(String key){
        //依靠配置文件解析解决${}格式问题
        PropertyExpr propertyExpr = parsePropertyExpr(key);
        if(propertyExpr != null){
            // 解析注解完成，查找值, 执行递归查找
            //所有getProperty都会调用本方法直到去掉所有${}，递归查询，支持嵌套注解${app.title:${APP_NAME:Summer}}
            if(propertyExpr.getDefaultValue() != null){
                // 有默认值
                return getProperty(propertyExpr.getKey(), propertyExpr.getDefaultValue());
            }
            else {
                //无默认值
                return getRequiredProperty(propertyExpr.getKey());
            }
        }
        // 普通查询,非${}格式
        String value = this.properties.get(key);
        if(value != null){
            // 查找到, 解决value中仍然含有一个${}的问题,即嵌套问题
            return parseValue(value);
        }
        return null;
    }

    // getProperty方法重载1,判断是否覆盖默认值
    public String getProperty(String key, String defaultValue){
        String value = getProperty(key);
        return value == null ? defaultValue: value;
    }
    // getProperty重载2，类型转换入口
    @NotNull
    public <T> T getProperty(String key, Class<T> targetType){
        String value = getProperty(key);
        if(value == null) return null;

        // 类型转换
        return convert(targetType, value);
    }

    // getProperty 重载3， 类型转换 + 默认值
    public <T> T getProperty(String key, Class<T> targetType, T defaultValue){
        String value = getProperty(key);
        if(value == null) return defaultValue;
        return convert(targetType, key);
    }

    //解析工具-按照${...}解析
    PropertyExpr parsePropertyExpr(String key){
        if(key.startsWith("${") && key.endsWith("}")){
            //判断是否有默认值 ${key:defaultValue}
            int index = key.indexOf(":");
            if(index == -1){
                //no defaultValue
                String k = notEmpty(key.substring(2,key.length()-1));
                return new PropertyExpr(k,null);
            }
            else {
                String k = notEmpty(key.substring(2,index));
                return new PropertyExpr(k,key.substring(index + 1,key.length()-1));
                // 解析默认值
            }
        }
        return null;
    }
    //按照${xx.xx.xx:defaultValue}解析
    String parseValue(String value){
        PropertyExpr expr = parsePropertyExpr(value);
        if (expr == null){
            // 不含有${}，返回原值
            return value;
        }
        // 递归解析key
        if(expr.getDefaultValue() != null){
            return getProperty(expr.getKey(), expr.getDefaultValue());
        }
        else {
            //无默认值
            return getRequiredProperty(expr.getKey());
        }

    }
    //required 系列 向外暴露，用户可自定义返回类型
    // required 1
    public String getRequiredProperty(String key){
        String value = getProperty(key);
        // value为空返回第二个参数的信息
        return Objects.requireNonNull(value, "Property'" + key + "' not found");
    }
    // required 2
    public <T> T getRequiredProperty(String key, Class<T> targetType){
        T value = getProperty(key,targetType);
        return Objects.requireNonNull(value,"Property'" + key + "' not found");
    }
    // 类型转换
    @SuppressWarnings("unchecked")
    <T> T convert(Class<?> clazz, String value){
        Function<String, Object> fn = this.converters.get(clazz);
        if(fn == null){
            throw new IllegalArgumentException("Unsupported value type:" + clazz.getName());
        }
        return (T) fn.apply(value);
    }

    // parsePropertyExpr使用, 保证截取的key有效
    String notEmpty(String key){
        if(key.isEmpty()){
            throw new IllegalArgumentException("Invalid key" + key);
        }
        return key;
    }

}
