package io.effi.rpc.logging;

/**
 * Logs messages at various levels with formatting support.
 * <p>
 * Provides a unified logging interface with level-based logging methods
 * and parameterized message formatting for framework logging.
 */
public interface Logger {

    /**
     * Checks if TRACE level logging is enabled.
     */
    boolean isTraceEnabled();

    /**
     * Logs a TRACE level message with an exception.
     *
     * @param format message format
     * @param e      throwable to log
     * @param args   format arguments
     */
    void trace(String format, Throwable e, Object... args);

    /**
     * Logs a TRACE level message.
     *
     * @param format message format
     * @param args   format arguments
     */
    void trace(String format, Object... args);

    /**
     * Logs a TRACE level exception.
     *
     * @param e throwable to log
     */
    void trace(Throwable e);

    /**
     * Checks if DEBUG level logging is enabled.
     */
    boolean isDebugEnabled();

    /**
     * Logs a DEBUG level message with an exception.
     *
     * @param format message format
     * @param e      throwable to log
     * @param args   format arguments
     */
    void debug(String format, Throwable e, Object... args);

    /**
     * Logs a DEBUG level message.
     *
     * @param format message format
     * @param args   format arguments
     */
    void debug(String format, Object... args);

    /**
     * Logs a DEBUG level exception.
     *
     * @param e throwable to log
     */
    void debug(Throwable e);

    /**
     * Checks if INFO level logging is enabled.
     */
    boolean isInfoEnabled();

    /**
     * Logs an INFO level message with an exception.
     *
     * @param format message format
     * @param e      throwable to log
     * @param args   format arguments
     */
    void info(String format, Throwable e, Object... args);

    /**
     * Logs an INFO level message.
     *
     * @param format message format
     * @param args   format arguments
     */
    void info(String format, Object... args);

    /**
     * Logs an INFO level exception.
     *
     * @param e throwable to log
     */
    void info(Throwable e);

    /**
     * Checks if WARN level logging is enabled.
     */
    boolean isWarnEnabled();

    /**
     * Logs a WARN level message with an exception.
     *
     * @param format message format
     * @param e      throwable to log
     * @param args   format arguments
     */
    void warn(String format, Throwable e, Object... args);

    /**
     * Logs a WARN level message.
     *
     * @param format message format
     * @param args   format arguments
     */
    void warn(String format, Object... args);

    /**
     * Logs a WARN level exception.
     *
     * @param e throwable to log
     */
    void warn(Throwable e);

    /**
     * Checks if ERROR level logging is enabled.
     */
    boolean isErrorEnabled();

    /**
     * Logs an ERROR level message with an exception.
     *
     * @param format message format
     * @param e      throwable to log
     * @param args   format arguments
     */
    void error(String format, Throwable e, Object... args);

    /**
     * Logs an ERROR level message.
     *
     * @param format message format
     * @param args   format arguments
     */
    void error(String format, Object... args);

    /**
     * Logs an ERROR level exception.
     *
     * @param e throwable to log
     */
    void error(Throwable e);
}
