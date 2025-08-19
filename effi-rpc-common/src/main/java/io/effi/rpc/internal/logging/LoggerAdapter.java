package io.effi.rpc.internal.logging;

/**
 * Provides logger instances based on specified names.
 * <p>
 * Serves as a factory interface for obtaining logger implementations
 * by name for internal logging purposes.
 */
public interface LoggerAdapter {

    /**
     * Retrieves the logger for the specified name.
     */
    Logger getLogger(String name);
}


