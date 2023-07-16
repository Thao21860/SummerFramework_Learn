package org.example.exception;

import org.example.annotation.Value;

public class UnsatisfiedDependencyException extends BeansException{
    int key;
    public UnsatisfiedDependencyException(String msg) {
        super(msg);
    }

    public UnsatisfiedDependencyException() {
    }
}
