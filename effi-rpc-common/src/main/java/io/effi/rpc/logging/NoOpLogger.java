package io.effi.rpc.logging;

final class NoOpLogger extends AbstractLogger {

    static final NoOpLogger INSTANCE = new NoOpLogger();

    private NoOpLogger() {
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public boolean isInfoEnabled() {
        return false;
    }

    @Override
    public boolean isWarnEnabled() {
        return false;
    }

    @Override
    public boolean isErrorEnabled() {
        return false;
    }

    @Override
    protected void trace(String msg, Throwable e) {
    }

    @Override
    protected void debug(String msg, Throwable e) {
    }

    @Override
    protected void info(String msg, Throwable e) {
    }

    @Override
    protected void warn(String msg, Throwable e) {
    }

    @Override
    protected void error(String msg, Throwable e) {
    }
}
