package io.effi.rpc.exception;

import io.effi.rpc.util.AssertUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

/**
 * Wraps error codes with formatted messages for RPC exceptions.
 * <p>
 * Provides a standardized exception type that encapsulates error codes and their
 * formatted messages, with support for unwrapping various exception types.
 */
public class EffiRpcException extends RuntimeException {

    private final ErrorCode errorCode;

    EffiRpcException(ErrorCode errorCode, Throwable e, Object... args) {
        super(errorCode.render(args), e);
        this.errorCode = errorCode;
    }

    public static EffiRpcException wrap(ErrorCode errorCode, Object... args) {
        return wrap(errorCode, null, args);
    }

    public static EffiRpcException wrap(ErrorCode errorCode, Throwable wrapped, Object... args) {
        AssertUtil.notNull(errorCode, "error code");
        if (wrapped == null) {
            return new EffiRpcException(errorCode, null, args);
        }
        while (true) {
            if (wrapped instanceof InvocationTargetException e) {
                wrapped = e.getTargetException();
            } else if (wrapped instanceof UndeclaredThrowableException e) {
                wrapped = e.getUndeclaredThrowable();
            } else if (wrapped instanceof ExecutionException e) {
                wrapped = e.getCause();
            } else if (wrapped instanceof CompletionException e) {
                wrapped = e.getCause();
            } else if (wrapped instanceof EffiRpcException e) {
                return e;
            } else {
                Throwable cause = wrapped.getCause();
                return new EffiRpcException(errorCode, cause == null ? wrapped : cause, args);
            }
        }
    }

    public CompletionException toCompletionException() {
        return new CompletionException(this.getMessage(), this.getCause());
    }

    public ErrorCode errorCode() {
        return errorCode;
    }
}

