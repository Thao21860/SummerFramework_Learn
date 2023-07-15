package org.example.exception;

public class BeanCreationException extends BeansException{
    public BeanCreationException(Throwable cause) {
        super(cause);
    }

    public BeanCreationException(String message) {
        super(message);
    }

    public BeanCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
