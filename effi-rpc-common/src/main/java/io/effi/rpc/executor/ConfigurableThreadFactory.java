package io.effi.rpc.executor;

import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ConfigurableThreadFactory implements ThreadFactory {

    private static final Thread.UncaughtExceptionHandler DEFAULT_EXCEPTION_HANDLER = new DefaultUncaughtExceptionHandler();

    private String namePrefix;

    private Boolean daemon;

    private Integer priority;

    private Thread.UncaughtExceptionHandler exceptionHandler;

    private final AtomicInteger threadNumber = new AtomicInteger(1);

    @Override
    public Thread newThread(Runnable task) {
        String finalNamePrefix = (namePrefix != null) ? namePrefix : "thread";
        Thread t = new Thread(task, finalNamePrefix + "-" + threadNumber.getAndIncrement());
        if (daemon != null) t.setDaemon(daemon);
        if (priority != null &&
                priority >= Thread.MIN_PRIORITY &&
                priority <= Thread.MAX_PRIORITY) {
            t.setPriority(priority);
        }
        Thread.UncaughtExceptionHandler uncaughtExceptionHandler = exceptionHandler == null
                ? DEFAULT_EXCEPTION_HANDLER
                : exceptionHandler;
        t.setUncaughtExceptionHandler(uncaughtExceptionHandler);
        return t;
    }

    public ConfigurableThreadFactory namePrefix(String namePrefix) {
        this.namePrefix = namePrefix;
        return this;
    }

    public ConfigurableThreadFactory daemon(Boolean daemon) {
        this.daemon = daemon;
        return this;
    }

    public ConfigurableThreadFactory priority(Integer priority) {
        this.priority = priority;
        return this;
    }

    public ConfigurableThreadFactory exceptionHandler(Thread.UncaughtExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return this;
    }


    public static class DefaultUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

        private static final Logger logger = LoggerFactory.getLogger(DefaultUncaughtExceptionHandler.class);

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            logger.error("Uncaught exception in thread [{}]: {}", e, t.getName());
        }
    }

}

