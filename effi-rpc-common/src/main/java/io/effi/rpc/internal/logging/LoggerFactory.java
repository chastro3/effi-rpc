package io.effi.rpc.internal.logging;

import java.util.List;
import java.util.concurrent.Callable;

import static io.effi.rpc.common.util.ObjectUtil.simpleClassName;

/**
 * Provides logger instances using various supported logging frameworks
 * (e.g., SLF4J, Log4j2, JCL, JDK logging). It automatically selects
 * an appropriate LoggerAdapter and provides a logger for the specified
 * class or name.
 */
public final class LoggerFactory {

    private static final LoggerAdapter CURRENT_ADAPTER;

    static {
        List<Callable<LoggerAdapter>> supportedAdapters = List.of(
                Sl4jLoggerAdapter::new,
                Log4j2LoggerAdapter::new,
                JclLoggerAdapter::new,
                JdkLoggerAdapter::new
        );
        // List of supported LoggerAdapters in the priority order
        CURRENT_ADAPTER = findUsableLoggerAdapter(supportedAdapters);
        if (CURRENT_ADAPTER == null) {
            System.err.println("No LoggerAdapter found");
        }
    }

    public static Logger getLogger(Class<?> type) {
        return getLogger(type.getName());
    }

    public static Logger getLogger(String name) {
        return CURRENT_ADAPTER.getLogger(name);
    }

    private static LoggerAdapter findUsableLoggerAdapter(List<Callable<LoggerAdapter>> supportedAdapters) {
        for (Callable<LoggerAdapter> creator : supportedAdapters) {
            try {
                // Try to create a LoggerAdapter instance
                LoggerAdapter loggerAdapter = creator.call();
                loggerAdapter.getLogger(LoggerFactory.class.getName()).debug("Using LoggerAdapter: {}", simpleClassName(loggerAdapter));
                return loggerAdapter;
            } catch (Throwable ignored) {
                // Ignore any exceptions and try the next adapter
            }
        }
        return null;
    }
}
