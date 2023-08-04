package org.example.exception;

public class BeanNotOfRequiredTypeException extends BeansException{
    public BeanNotOfRequiredTypeException(String message) {
        super(message);
    }
}
