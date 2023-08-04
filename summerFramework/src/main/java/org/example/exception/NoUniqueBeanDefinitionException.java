package org.example.exception;

public class NoUniqueBeanDefinitionException extends BeansException{
    public NoUniqueBeanDefinitionException() {
    }

    public NoUniqueBeanDefinitionException(String message) {
        super(message);
    }
}
