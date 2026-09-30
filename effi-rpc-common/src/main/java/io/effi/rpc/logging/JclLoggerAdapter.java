package io.effi.rpc.logging;

import org.apache.commons.logging.LogFactory;

class JclLoggerAdapter implements LoggerAdapter {

    @Override
    public String name() {
        return "jcl";
    }

    @Override
    public int priority() {
        return 80;
    }

    @Override
    public Logger getLogger(String name) {
        return new JclLogger(LogFactory.getLog(name));
    }
}
