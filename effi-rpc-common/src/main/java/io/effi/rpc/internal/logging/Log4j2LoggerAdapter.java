package io.effi.rpc.internal.logging;

import org.apache.logging.log4j.LogManager;

class Log4j2LoggerAdapter implements LoggerAdapter {

    Log4j2LoggerAdapter() throws ClassNotFoundException {
        Class.forName(LogManager.class.getName());
    }

    @Override
    public Logger getLogger(String name) {
        return new Log4j2Logger(LogManager.getLogger(name));
    }
}
