package io.effi.rpc.executor;

import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;

/**
 * ThreadExceptionHandler.
 */
public class ThreadExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ThreadExceptionHandler.class);

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        logger.error(t.getName() + " occur error", e);
    }

}
