package io.effi.rpc.logging;

import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPLogger;

class Slf4jLoggerAdapter implements LoggerAdapter {

    @Override
    public String name() {
        return "slf4j";
    }

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public Logger getLogger(String name) {
        org.slf4j.Logger logger = LoggerFactory.getLogger(name);
        // Skip SLF4J when no binding is configured so the next adapter can take over.
        if (logger instanceof NOPLogger) {
            throw new IllegalStateException("SLF4J is present but no binding is configured");
        }
        return new Slf4jLogger(logger);
    }
}
