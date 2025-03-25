package io.effi.rpc.internal.logging;

import java.util.logging.Level;
import java.util.logging.Logger;

class JdkLogger extends AbstractLogger {

    private final Logger logger;

    JdkLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    protected void trace(String msg, Throwable e) {
        logger.log(Level.FINEST, msg, e);
    }

    @Override
    protected void debug(String msg, Throwable e) {
        logger.log(Level.FINE, msg, e);
    }

    @Override
    protected void info(String msg, Throwable e) {
        logger.log(Level.INFO, msg, e);
    }

    @Override
    protected void warn(String msg, Throwable e) {
        logger.log(Level.WARNING, msg, e);
    }

    @Override
    protected void error(String msg, Throwable e) {
        logger.log(Level.SEVERE, msg, e);
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isLoggable(Level.FINEST);
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isLoggable(Level.FINE);
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isLoggable(Level.INFO);
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isLoggable(Level.WARNING);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isLoggable(Level.SEVERE);
    }
}
