package io.effi.rpc.internal.logging;

import static io.effi.rpc.common.util.ObjectUtil.simpleClassName;

/**
 * Provide logger instances using various supported logging frameworks
 * (e.g., SLF4J, Log4j2, JCL, JDK logging). It automatically selects
 * an appropriate LoggerAdapter and provides a logger for the specified
 * class or name.
 */
public class LoggerFactory {

    // Holds the selected LoggerAdapter instance
    private static final LoggerAdapter LOGGER_ADAPTER;

    // Static block initializes the LOGGER_ADAPTER at class loading time
    static {
        // List of supported LoggerAdapters in the priority order
        LoggerAdapterCreator[] supportedAdapters = {
                Sl4jLoggerAdapter::new,
                Log4j2LoggerAdapter::new,
                JclLoggerAdapter::new,
                JdkLoggerAdapter::new
        };
        LOGGER_ADAPTER = getLoggerAdapter(supportedAdapters);
        if (LOGGER_ADAPTER == null) {
            System.err.println("No LoggerAdapter found");
        } else {
            LOGGER_ADAPTER.getLogger(LoggerFactory.class.getName()).info("Using LoggerAdapter: {}", simpleClassName(LOGGER_ADAPTER));
        }
    }

    /**
     * Retrieves a logger for the specified class type.
     *
     * @param type the class type for which the logger is needed
     * @return the logger instance for the specified class
     */
    public static Logger getLogger(Class<?> type) {
        return getLogger(type.getName());
    }

    /**
     * Retrieves a logger for the specified name.
     *
     * @param name the name for which the logger is needed
     * @return the logger instance for the specified name
     */
    public static Logger getLogger(String name) {
        return LOGGER_ADAPTER.getLogger(name);
    }

    /**
     * Attempts to create a LoggerAdapter from the list of supported adapters.
     *
     * @param supportedAdapters the list of supported LoggerAdapter creators
     * @return the first valid LoggerAdapter found, or null if none are available
     */
    private static LoggerAdapter getLoggerAdapter(LoggerAdapterCreator[] supportedAdapters) {
        for (LoggerAdapterCreator creator : supportedAdapters) {
            try {
                // Try to create a LoggerAdapter instance
                return creator.create();
            } catch (Throwable ignored) {
                // Ignore any exceptions and try the next adapter
            }
        }
        return null;
    }

    /**
     * Functional interface representing a creator for LoggerAdapter.
     */
    @FunctionalInterface
    private interface LoggerAdapterCreator {
        /**
         * Creates a LoggerAdapter instance.
         *
         * @return the created LoggerAdapter
         * @throws Throwable if the creation fails
         */
        LoggerAdapter create() throws Throwable;
    }

}
