package io.effi.rpc.logging;

import org.slf4j.Logger;

class Slf4jLogger extends AbstractLogger {

    private final Logger logger;

    Slf4jLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    protected void trace(String msg, Throwable e) {
        logger.trace(msg, e);
    }

    @Override
    protected void debug(String msg, Throwable e) {
        logger.debug(msg, e);
    }

    @Override
    protected void info(String msg, Throwable e) {
        logger.info(msg, e);
    }

    @Override
    protected void warn(String msg, Throwable e) {
        logger.warn(msg, e);
    }

    @Override
    protected void error(String msg, Throwable e) {
        logger.error(msg, e);
    }
}
