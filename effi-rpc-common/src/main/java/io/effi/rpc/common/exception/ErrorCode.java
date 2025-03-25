package io.effi.rpc.common.exception;

import io.effi.rpc.common.util.StringUtil;

/**
 * Represents an error code with its associated message.
 */
public interface ErrorCode {

    /**
     * Gets the error code.
     *
     * @return the error code
     */
    String code();

    /**
     * Gets the error message.
     *
     * @return the error message
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


