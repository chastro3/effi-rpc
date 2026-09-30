package io.effi.rpc.logging;

/**
 * Defines the SPI for supplying framework loggers.
 * <p>
 * Implementations are discovered through {@link java.util.ServiceLoader}. Register a provider in
 * {@code META-INF/services/io.effi.rpc.logging.LoggerAdapter} to make it available to
 * {@link LoggerFactory}. Higher-priority adapters are selected first.
 */
public interface LoggerAdapter {

    /**
     * Specifies the adapter name.
     */
    String name();

    /**
     * Specifies the selection priority.
     */
    default int priority() {
        return 0;
    }

    /**
     * Supplies a logger for the given name.
     *
     * @param name logger name
     * @return logger instance
     */
    Logger getLogger(String name);
}
