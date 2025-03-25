package io.effi.rpc.internal.logging;

import org.apache.commons.logging.Log;

class JclLogger extends AbstractLogger {

    private final Log log;

    JclLogger(Log log) {
        this.log = log;
    }

    @Override
    protected void trace(String msg, Throwable e) {
        log.trace(msg, e);
    }

    @Override
    protected void debug(String msg, Throwable e) {
        log.debug(msg, e);
    }

    @Override
    protected void info(String msg, Throwable e) {
        log.info(msg, e);
    }

    @Override
    protected void warn(String msg, Throwable e) {
        log.warn(msg, e);
    }

    @Override
    protected void error(String msg, Throwable e) {
        log.error(msg, e);
    }

    @Override
    public boolean isTraceEnabled() {
        return log.isTraceEnabled();
    }

    @Override
    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return log.isWarnEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return log.isErrorEnabled();
    }
}
