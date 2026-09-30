package io.effi.rpc.logging;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * Provides logger instances through pluggable logging adapters.
 * <p>
 * Adapters are discovered through {@link ServiceLoader} and combined with built-in SLF4J, Log4j2,
 * JCL, and JDK logging adapters. The highest-priority available adapter is selected; if none is
 * available, a no-op adapter is used. The first selection and explicit adapter switches are logged
 * at DEBUG through the selected adapter.
 */
public final class LoggerFactory {

    private static final List<LoggerAdapter> BUILT_IN_ADAPTERS = List.of(
            new Slf4jLoggerAdapter(),
            new Log4j2LoggerAdapter(),
            new JclLoggerAdapter(),
            new JdkLoggerAdapter()
    );

    private static volatile LoggerAdapter currentAdapter;

    private LoggerFactory() {
    }

    /**
     * Returns a logger for the given type.
     *
     * @param type logger type
     * @return logger instance
     */
    public static Logger getLogger(Class<?> type) {
        return getLogger(type.getName());
    }

    /**
     * Returns a logger for the given name.
     *
     * @param name logger name
     * @return logger instance
     */
    public static Logger getLogger(String name) {
        return adapter().getLogger(name);
    }

    /**
     * Installs a custom adapter.
     * <p>
     * Intended for bootstrap integration and tests. The next {@link #getLogger(String)} call uses
     * the installed adapter.
     *
     * @param adapter custom adapter
     */
    public static void setAdapter(LoggerAdapter adapter) {
        LoggerAdapter selected = Objects.requireNonNull(adapter, "adapter");
        synchronized (LoggerFactory.class) {
            if (currentAdapter != selected) {
                currentAdapter = selected;
                logAdapterSelection("switched", selected);
            }
        }
    }

    /**
     * Clears the selected adapter.
     * <p>
     * The next lookup re-selects an adapter from registered SPI providers and built-in adapters.
     */
    public static void clearAdapter() {
        synchronized (LoggerFactory.class) {
            currentAdapter = null;
        }
    }

    private static LoggerAdapter adapter() {
        LoggerAdapter selected = currentAdapter;
        if (selected != null) {
            return selected;
        }
        // Guard the one-time adapter selection without synchronizing every lookup.
        synchronized (LoggerFactory.class) {
            if (currentAdapter == null) {
                currentAdapter = selectAdapter();
                logAdapterSelection("selected", currentAdapter);
            }
            return currentAdapter;
        }
    }

    private static LoggerAdapter selectAdapter() {
        List<LoggerAdapter> adapters = new ArrayList<>();
        loadSpiAdapters(adapters);
        adapters.addAll(BUILT_IN_ADAPTERS);
        return adapters.stream()
                .filter(LoggerFactory::available)
                .max(Comparator.comparingInt(LoggerAdapter::priority))
                .orElse(NoOpLoggerAdapter.INSTANCE);
    }

    // Skip malformed providers so one broken SPI entry cannot disable logging.
    private static void loadSpiAdapters(List<LoggerAdapter> adapters) {
        Iterator<LoggerAdapter> iterator = ServiceLoader.load(LoggerAdapter.class).iterator();
        while (true) {
            try {
                if (!iterator.hasNext()) {
                    return;
                }
                adapters.add(iterator.next());
            } catch (ServiceConfigurationError ignored) {
                // Continue with the next provider.
            }
        }
    }

    // Probe adapter availability so optional logging backends can be skipped safely.
    private static boolean available(LoggerAdapter adapter) {
        try {
            return adapter.getLogger(LoggerFactory.class.getName()) != null;
        } catch (Exception | LinkageError ignored) {
            return false;
        }
    }

    // Diagnostic logging must not interfere with adapter selection.
    private static void logAdapterSelection(String action, LoggerAdapter adapter) {
        try {
            adapter.getLogger(LoggerFactory.class.getName())
                    .debug("[effi-rpc] Logging adapter {}: {}", action, adapter.name());
        } catch (Exception | LinkageError ignored) {
            // Ignore diagnostic logging failures when selecting an adapter.
        }
    }
}
