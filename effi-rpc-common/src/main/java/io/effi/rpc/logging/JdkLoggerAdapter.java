package io.effi.rpc.logging;

class JdkLoggerAdapter implements LoggerAdapter {

    @Override
    public String name() {
        return "jdk";
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public Logger getLogger(String name) {
        return new JdkLogger(java.util.logging.Logger.getLogger(name));
    }
}
