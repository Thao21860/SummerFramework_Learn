package org.example.exception;

import io.basc.framework.sql.Sql;
import io.basc.framework.sql.SqlException;

public class DataAccessException extends SqlException {
    public DataAccessException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public DataAccessException(Sql sql, Throwable cause) {
        super(sql, cause);
    }

    public DataAccessException(String msg) {
        super(msg);
    }

    public DataAccessException(Throwable cause) {
        super(cause);
    }
}
