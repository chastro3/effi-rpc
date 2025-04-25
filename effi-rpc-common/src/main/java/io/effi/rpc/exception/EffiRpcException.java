package io.effi.rpc.exception;

import io.effi.rpc.util.AssertUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.ExecutionException;

/**
 * Custom exception class for Effi-RPC.
 * Wraps an {@link ErrorCode} and supports formatted messages with arguments.
 */
public class EffiRpcException extends RuntimeException {

    private final ErrorCode errorCode;

    EffiRpcException(ErrorCode errorCode, Throwable e, Object... args) {
        super(errorCode.convert(args), e);
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
            switch (wrapped) {
                case InvocationTargetException e -> wrapped = e.getTargetException();
                case UndeclaredThrowableException e -> wrapped = e.getUndeclaredThrowable();
                case ExecutionException e -> wrapped = e.getCause();
                case EffiRpcException e -> {
                    return e;
                }
                default -> {
                    return new EffiRpcException(
                            errorCode,
                            wrapped.getCause() == null ? wrapped : wrapped.getCause(),
                            args
                    );
                }
            }
        }
    }

    public ErrorCode errorCode() {
        return errorCode;
    }
}

