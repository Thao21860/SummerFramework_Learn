package org.summer.exception;

public class UnsatisfiedDependencyException extends BeansException{
    int key;
    public UnsatisfiedDependencyException(String msg) {
        super(msg);
    }

    public UnsatisfiedDependencyException() {
    }
}
