package org.summer.exception;

public class DataAccessException extends NestedRuntimeException {

    public DataAccessException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public DataAccessException(String msg) {
        super(msg);
    }

    public DataAccessException(Throwable cause) {
        super(cause);
    }

    public DataAccessException() {

    }
}
