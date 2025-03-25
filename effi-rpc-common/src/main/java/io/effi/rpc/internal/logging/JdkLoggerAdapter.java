package io.effi.rpc.internal.logging;

class JdkLoggerAdapter implements LoggerAdapter {
    @Override
    public Logger getLogger(String name) {
        return new JdkLogger(java.util.logging.Logger.getLogger(name));
    }
}
