package io.effi.rpc.exception;

import io.effi.rpc.util.AssertUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Map;
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

    private final Map<String, String> metadata;

    EffiRpcException(ErrorCode errorCode, Throwable e, Object... args) {
        this(errorCode, errorCode.render(args), e, Map.of());
    }

    private EffiRpcException(ErrorCode errorCode, String message, Throwable cause, Map<String, String> metadata) {
        super(message, cause);
        this.errorCode = errorCode;
        this.metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    /**
     * Creates an RPC exception from an error code and message arguments.
     *
     * @param errorCode error code
     * @param args message arguments
     * @return RPC exception
     */
    public static EffiRpcException wrap(ErrorCode errorCode, Object... args) {
        return wrap(errorCode, null, args);
    }

    /**
     * Creates an RPC exception from an error code, wrapped failure, and message arguments.
     *
     * @param errorCode error code
     * @param wrapped wrapped failure, or {@code null} when absent
     * @param args message arguments
     * @return RPC exception with the effective cause
     */
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

    /**
     * Adds or replaces protocol metadata while preserving the error code, message, and cause.
     *
     * @param metadata protocol metadata
     * @return exception carrying the supplied metadata
     */
    public EffiRpcException withMetadata(Map<String, String> metadata) {
        return new EffiRpcException(errorCode, getMessage(), getCause(), metadata);
    }

    /**
     * Returns a completion exception wrapping this exception.
     */
    public CompletionException toCompletionException() {
        return new CompletionException(this);
    }

    /**
     * Returns the error code.
     */
    public ErrorCode errorCode() {
        return errorCode;
    }

    /**
     * Returns immutable protocol metadata such as gRPC trailers.
     */
    public Map<String, String> metadata() {
        return metadata;
    }
}

