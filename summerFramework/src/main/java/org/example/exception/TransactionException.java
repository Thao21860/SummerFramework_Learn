package org.example.exception;

public class TransactionException extends DataAccessException{
    public TransactionException(){
        super();
    };
    public TransactionException(String message) {
        super(message);
    }

    public TransactionException(Throwable cause) {
        super(cause);
    }

    public TransactionException(String message, Throwable cause) {
        super(message, cause);
    }

}
