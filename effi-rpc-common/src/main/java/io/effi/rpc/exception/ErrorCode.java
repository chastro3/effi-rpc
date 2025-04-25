package io.effi.rpc.exception;

import io.effi.rpc.util.StringUtil;

/**
 * Represents an error code with its associated message.
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
     * Formats the error message with the provided arguments.
     *
     * @param args arguments to format the message
     * @return the formatted error message
     */
    default String convert(Object... args) {
        return StringUtil.format(message(), args);
    }
}


