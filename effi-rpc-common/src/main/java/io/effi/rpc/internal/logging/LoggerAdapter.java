package io.effi.rpc.internal.logging;

/**
 * Retrieve logger instances based on a given name. Implementations of this
 * interface can supply loggers that are suitable for different logging frameworks
 * or custom logging systems.
 */
public interface LoggerAdapter {

    /**
     * Retrieves a logger instance associated with the given name.
     *
     * @param name the name of the logger to retrieve
     * @return the logger instance for the specified name
     */
    Logger getLogger(String name);
}

