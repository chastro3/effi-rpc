package io.effi.rpc.logging;

final class NoOpLoggerAdapter implements LoggerAdapter {

    static final NoOpLoggerAdapter INSTANCE = new NoOpLoggerAdapter();

    private NoOpLoggerAdapter() {
    }

    @Override
    public String name() {
        return "noop";
    }

    @Override
    public int priority() {
        return Integer.MIN_VALUE;
    }

    @Override
    public Logger getLogger(String name) {
        return NoOpLogger.INSTANCE;
    }
}
