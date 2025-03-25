package io.effi.rpc.common.exception;

import io.effi.rpc.common.util.AssertUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.ExecutionException;

/**
 * Custom exception class for Effi-RPC.
 * Wraps an {@link ErrorCode} and supports formatted messages with arguments.
 */
public class EffiRpcException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * Constructs a new {@code EffiRpcException} with the specified error code, cause, and message arguments.
     *
     * @param errorCode the error code representing the error type.
     * @param e         the underlying cause of the exception (nullable).
     * @param args      additional arguments to format the error message.
     */
    EffiRpcException(ErrorCode errorCode, Throwable e, Object... args) {
        super(errorCode.convert(args), e);
        this.errorCode = errorCode;
    }

    /**
     * Creates a new {@code EffiRpcException} with the given error code and optional message arguments.
     *
     * @param errorCode the error code.
     * @param args      optional arguments to format the error message.
     * @return a new instance of {@code EffiRpcException}.
     */
    public static EffiRpcException wrap(ErrorCode errorCode, Object... args) {
        return wrap(errorCode, null, args);
    }

    /**
     * Wraps an existing exception into an {@code EffiRpcException}, preserving the root cause.
     * <p>
     * If the given exception is already an {@code EffiRpcException}, it will be returned as is.
     * Otherwise, the method will unwrap common wrapper exceptions (such as {@link InvocationTargetException}
     * and {@link ExecutionException}) to extract the root cause.
     *
     * @param errorCode the error code associated with the new exception.
     * @param wrapped   the exception to wrap (nullable).
     * @param args      optional arguments to format the error message.
     * @return a new or existing instance of {@code EffiRpcException}.
     */
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

    /**
     * Returns the associated error code.
     *
     * @return the error code.
     */
    public ErrorCode errorCode() {
        return errorCode;
    }
}

