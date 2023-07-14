package org.example.config;

import java.util.Objects;

//解析配置文件
public class PropertyExpr {
    private final String key;
    private final String defaultValue;

    public PropertyExpr(String key, String defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public String getKey() {
        return key;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String toString() {
        return "PropertyExpr{" +
                "key='" + key + '\'' +
                ", defaultValue='" + defaultValue + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        PropertyExpr propertyExpr = (PropertyExpr) obj;
        return Objects.equals(key, propertyExpr.key) && Objects.equals(defaultValue,propertyExpr.defaultValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, defaultValue);
    }
}
