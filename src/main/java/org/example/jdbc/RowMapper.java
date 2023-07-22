package org.example.jdbc;

import jakarta.annotation.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
// 用于从rs中获取对应类型数据
public interface RowMapper<T> {
    @Nullable
    T mapRow(ResultSet rs, int rowNum) throws SQLException;
}
