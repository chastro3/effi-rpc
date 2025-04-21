package io.effi.rpc.internal.logging;

import org.apache.commons.logging.LogFactory;

class JclLoggerAdapter implements LoggerAdapter {

    @Override
    public Logger getLogger(String name) {
        return new JclLogger(LogFactory.getLog(name));
    }
}
