package io.effi.rpc.internal.logging;

/**
 * Logs messages at various levels.
 * <p><em>For internal use only.</em></p>
 */
public interface Logger {

    /**
     * Checks if TRACE level logging is enabled.
     */
    boolean isTraceEnabled();

    /**
     * Logs a TRACE level message with an exception.
     */
    void trace(String format, Throwable e, Object... args);

    /**
     * Logs a TRACE level message.
     */
    void trace(String format, Object... args);

    /**
     * Logs a TRACE level exception.
     */
    void trace(Throwable e);

    /**
     * Checks if DEBUG level logging is enabled.
     */
    boolean isDebugEnabled();

    /**
     * Logs a DEBUG level message with an exception.
     */
    void debug(String format, Throwable e, Object... args);

    /**
     * Logs a DEBUG level message.
     */
    void debug(String format, Object... args);

    /**
     * Logs a DEBUG level exception.
     */
    void debug(Throwable e);

    /**
     * Checks if INFO level logging is enabled.
     */
    boolean isInfoEnabled();

    /**
     * Logs an INFO level message with an exception.
     */
    void info(String format, Throwable e, Object... args);

    /**
     * Logs an INFO level message.
     */
    void info(String format, Object... args);

    /**
     * Logs an INFO level exception.
     */
    void info(Throwable e);

    /**
     * Checks if WARN level logging is enabled.
     */
    boolean isWarnEnabled();

    /**
     * Logs a WARN level message with an exception.
     */
    void warn(String format, Throwable e, Object... args);

    /**
     * Logs a WARN level message.
     */
    void warn(String format, Object... args);

    /**
     * Logs a WARN level exception.
     */
    void warn(Throwable e);

    /**
     * Checks if ERROR level logging is enabled.
     */
    boolean isErrorEnabled();

    /**
     * Logs an ERROR level message with an exception.
     */
    void error(String format, Throwable e, Object... args);

    /**
     * Logs an ERROR level message.
     */
    void error(String format, Object... args);

    /**
     * Logs an ERROR level exception.
     */
    void error(Throwable e);
}


