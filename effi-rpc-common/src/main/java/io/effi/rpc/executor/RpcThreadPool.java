package io.effi.rpc.executor;

import io.effi.rpc.constant.Constant;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Implements RPC-specific defaults for {@link ThreadPoolExecutor}.
 */
public class RpcThreadPool extends ThreadPoolExecutor {

    public RpcThreadPool(int corePoolSize, int maximumPoolSize, String namePrefix) {
        this(corePoolSize, maximumPoolSize,
                Constant.DEFAULT_KEEP_ALIVE, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(Constant.DEFAULT_CAPACITY),
                new ConfigurableThreadFactory().namePrefix(namePrefix).daemon(false),
                new AbortPolicy());
    }

    public RpcThreadPool(int corePoolSize, int maximumPoolSize,
                         long keepAliveTime, TimeUnit unit,
                         BlockingQueue<Runnable> workQueue,
                         ThreadFactory threadFactory,
                         RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    /**
     * Returns the default I/O thread pool.
     *
     * @param namePrefix worker thread name prefix
     * @return configured I/O executor
     */
    public static ExecutorService defaultIOExecutor(String namePrefix) {
        return new RpcThreadPool(Constant.DEFAULT_IO_THREADS, Constant.DEFAULT_MAX_IO_THREADS, namePrefix);
    }

    /**
     * Returns the default CPU thread pool.
     *
     * @param namePrefix worker thread name prefix
     * @return configured CPU executor
     */
    public static ExecutorService defaultCPUExecutor(String namePrefix) {
        return new RpcThreadPool(Constant.DEFAULT_CPU_THREADS, Constant.DEFAULT_MAX_CPU_THREADS, namePrefix);
    }
}
