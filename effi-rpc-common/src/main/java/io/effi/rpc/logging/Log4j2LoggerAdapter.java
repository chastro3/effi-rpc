package io.effi.rpc.logging;

import org.apache.logging.log4j.LogManager;

class Log4j2LoggerAdapter implements LoggerAdapter {

    @Override
    public String name() {
        return "log4j2";
    }

    @Override
    public int priority() {
        return 90;
    }

    @Override
    public Logger getLogger(String name) {
        return new Log4j2Logger(LogManager.getLogger(name));
    }
}
