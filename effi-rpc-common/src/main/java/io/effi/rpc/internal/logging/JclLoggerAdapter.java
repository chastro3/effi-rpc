package io.effi.rpc.internal.logging;

import org.apache.commons.logging.LogFactory;

class JclLoggerAdapter implements LoggerAdapter {

    JclLoggerAdapter() throws ClassNotFoundException {
        Class.forName(LogFactory.class.getName());
    }

    @Override
    public Logger getLogger(String name) {
        return new JclLogger(LogFactory.getLog(name));
    }
}
