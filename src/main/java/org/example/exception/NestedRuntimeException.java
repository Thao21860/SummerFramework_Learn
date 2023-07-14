package org.example.exception;
// 嵌套运行时异常
public class NestedRuntimeException extends RuntimeException{
    public NestedRuntimeException(){
    }
    public NestedRuntimeException(String message){
        super(message);
    }
    public NestedRuntimeException(Throwable cause){
        super(cause);
    }
    public NestedRuntimeException(String message, Throwable cause){
        super(message, cause);
    }
}
