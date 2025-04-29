package io.effi.rpc.exception;

import io.effi.rpc.util.StringUtil;

/**
 * Represents an error code and its associated message.
 */
public interface ErrorCode {

    /**
     * Gets the error code.
     */
    String code();

    /**
     * Gets the error message.
     */
    String message();

    /**
     * Formats the error message with the provided arguments.
     *
     * @param args arguments to format the message
     * @return the formatted message
     */
    default String convert(Object... args) {
        return StringUtil.format(message(), args);
    }
}



