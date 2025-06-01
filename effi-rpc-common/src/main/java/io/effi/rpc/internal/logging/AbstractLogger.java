package io.effi.rpc.internal.logging;

import io.effi.rpc.util.StringUtil;

abstract class AbstractLogger implements Logger {

    @Override
    public void trace(String format, Throwable e, Object... args) {
        if (isTraceEnabled()) {
            String msg = StringUtil.format(format, args);
            trace(msg, e);
        }
    }

    @Override
    public void trace(String format, Object... args) {
        trace(format, null, args);
    }

    @Override
    public void trace(Throwable e) {
        if (isTraceEnabled()) {
            trace(StringUtil.empty(), e);
        }
    }

    @Override
    public void debug(String format, Throwable e, Object... args) {
        if (isDebugEnabled()) {
            String msg = StringUtil.format(format, args);
            debug(msg, e);
        }
    }

    @Override
    public void debug(String format, Object... args) {
        debug(format, null, args);
    }

    @Override
    public void debug(Throwable e) {
        if (isDebugEnabled()) {
            debug(StringUtil.empty(), e);
        }
    }

    @Override
    public void info(String format, Throwable e, Object... args) {
        if (isInfoEnabled()) {
            String msg = StringUtil.format(format, args);
            info(msg, e);
        }
    }

    @Override
    public void info(String format, Object... args) {
        info(format, null, args);
    }

    @Override
    public void info(Throwable e) {
        if (isInfoEnabled()) {
            info(StringUtil.empty(), e);
        }
    }

    @Override
    public void warn(String format, Throwable e, Object... args) {
        if (isWarnEnabled()) {
            String msg = StringUtil.format(format, args);
            warn(msg, e);
        }
    }

    @Override
    public void warn(String format, Object... args) {
        warn(format, null, args);
    }

    @Override
    public void warn(Throwable e) {
        if (isWarnEnabled()) {
            warn(StringUtil.empty(), e);
        }
    }

    @Override
    public void error(String format, Throwable e, Object... args) {
        if (isErrorEnabled()) {
            String msg = StringUtil.format(format, args);
            error(msg, e);
        }
    }

    @Override
    public void error(String format, Object... args) {
        error(format, null, args);
    }

    @Override
    public void error(Throwable e) {
        if (isErrorEnabled()) {
            error(StringUtil.empty(), e);
        }
    }

    protected abstract void trace(String msg, Throwable e);

    protected abstract void debug(String msg, Throwable e);

    protected abstract void info(String msg, Throwable e);

    protected abstract void warn(String msg, Throwable e);

    protected abstract void error(String msg, Throwable e);
}
