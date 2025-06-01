package io.effi.rpc.internal.logging;

/**
 * Provides logger instance based on the given name.
 */
public interface LoggerAdapter {

    /**
     * Retrieves the logger for the specified name.
     */
    Logger getLogger(String name);
}


