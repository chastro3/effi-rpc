package io.effi.rpc.executor;

import io.effi.rpc.logging.Logger;
import io.effi.rpc.logging.LoggerFactory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Creates configurable RPC worker threads.
 */
public class ConfigurableThreadFactory implements ThreadFactory {

    private static final Thread.UncaughtExceptionHandler DEFAULT_EXCEPTION_HANDLER = new DefaultUncaughtExceptionHandler();
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private String namePrefix;
    private Boolean daemon;
    private Integer priority;
    private Thread.UncaughtExceptionHandler exceptionHandler;

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

    /**
     * Sets the worker thread name prefix.
     *
     * @param namePrefix thread name prefix
     * @return this factory
     */
    public ConfigurableThreadFactory namePrefix(String namePrefix) {
        this.namePrefix = namePrefix;
        return this;
    }

    /**
     * Sets whether created threads are daemon threads.
     *
     * @param daemon daemon flag, or {@code null} to inherit the current thread setting
     * @return this factory
     */
    public ConfigurableThreadFactory daemon(Boolean daemon) {
        this.daemon = daemon;
        return this;
    }

    /**
     * Sets the worker thread priority.
     *
     * @param priority thread priority, or {@code null} to inherit the current thread priority
     * @return this factory
     */
    public ConfigurableThreadFactory priority(Integer priority) {
        this.priority = priority;
        return this;
    }

    /**
     * Sets the uncaught exception handler for created threads.
     *
     * @param exceptionHandler uncaught exception handler
     * @return this factory
     */
    public ConfigurableThreadFactory exceptionHandler(Thread.UncaughtExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return this;
    }


    /**
     * Logs uncaught exceptions raised by RPC worker threads.
     */
    public static class DefaultUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

        private static final Logger logger = LoggerFactory.getLogger(DefaultUncaughtExceptionHandler.class);

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            logger.error("Uncaught exception in thread [{}]: {}", e, t.getName());
        }
    }

}

