package io.effi.rpc.internal.logging;

import java.util.List;
import java.util.concurrent.Callable;

import static io.effi.rpc.common.util.ObjectUtil.simpleClassName;

/**
 * Provide logger instances using various supported logging frameworks
 * (e.g., SLF4J, Log4j2, JCL, JDK logging). It automatically selects
 * an appropriate LoggerAdapter and provides a logger for the specified
 * class or name.
 */
public final class LoggerFactory {

    // Holds the selected LoggerAdapter instance
    private static final LoggerAdapter CURRENT_ADAPTER;

    // Static block initializes the LOGGER_ADAPTER at class loading time
    static {
        List<Callable<LoggerAdapter>> supportedAdapters = List.of(
                Sl4jLoggerAdapter::new,
                Log4j2LoggerAdapter::new,
                JclLoggerAdapter::new,
                JdkLoggerAdapter::new
        );
        // List of supported LoggerAdapters in the priority order
        CURRENT_ADAPTER = getLoggerFactory(supportedAdapters);
        if (CURRENT_ADAPTER == null) {
            System.err.println("No LoggerAdapter found");
        } else {
            CURRENT_ADAPTER.getLogger(LoggerFactory.class.getName()).info("Using LoggerAdapter: {}", simpleClassName(CURRENT_ADAPTER));
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
        return CURRENT_ADAPTER.getLogger(name);
    }

    private static LoggerAdapter getLoggerFactory(List<Callable<LoggerAdapter>> supportedAdapters) {
        for (Callable<LoggerAdapter> creator : supportedAdapters) {
            try {
                // Try to create a LoggerAdapter instance
                return creator.call();
            } catch (Throwable ignored) {
                // Ignore any exceptions and try the next adapter
            }
        }
        return null;
    }
}
