package org.example.jdbc;

import jakarta.annotation.Nullable;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementCallback<T>{
    @Nullable
    T doInConnection(PreparedStatement ps) throws SQLException;
}
