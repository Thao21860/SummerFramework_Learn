package org.summer.exception;

public class AopConfigException extends BeansException{
    public AopConfigException() {
        super();
    }

    public AopConfigException(String message) {
        super(message);
    }

    public AopConfigException(Throwable cause) {
        super(cause);
    }

    public AopConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
