package io.effi.rpc.exception;

import io.effi.rpc.util.StringUtil;

/**
 * Represents error codes with associated messages and formatting capabilities.
 * <p>
 * Provides a structured way to define error codes along with their messages,
 * supporting message formatting and exception creation.
 */
public interface ErrorCode {

    /**
     * Returns the error code.
     */
    String code();

    /**
     * Returns the error message.
     */
    String message();

    /**
     * Renders the message using the provided arguments.
     *
     * @param args the arguments to format the message
     * @return the formatted message
     */
    default String render(Object... args) {
        return StringUtil.format(message(), args);
    }

    /**
     * Creates an {@link EffiRpcException} with this error code and the provided arguments.
     *
     * @param args the arguments to format the message
     * @return the created exception
     */
    default EffiRpcException fail(Throwable cause, Object... args) {
        return EffiRpcException.wrap(this, cause, args);
    }

    default EffiRpcException fail(Object... args) {
        return fail(null, args);
    }
}



