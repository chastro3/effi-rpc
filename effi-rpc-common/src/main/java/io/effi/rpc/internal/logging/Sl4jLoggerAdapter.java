package io.effi.rpc.internal.logging;

import org.slf4j.LoggerFactory;

class Sl4jLoggerAdapter implements LoggerAdapter {

    @Override
    public Logger getLogger(String name) {
        return new Sl4jLogger(LoggerFactory.getLogger(name));
    }
}
