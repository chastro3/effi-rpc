package io.effi.rpc.constant;

/**
 * Holds constants for default configuration.
 */
public interface Constant {

    String DEFAULT_NAME = "default";

    int DEFAULT_IO_THREADS = Math.min(Runtime.getRuntime().availableProcessors() + 1, 32);

    int DEFAULT_CPU_THREADS = Runtime.getRuntime().availableProcessors();

    int DEFAULT_MAX_IO_THREADS = Runtime.getRuntime().availableProcessors() * 5;

    int DEFAULT_MAX_CPU_THREADS = Runtime.getRuntime().availableProcessors() * 2;

    int DEFAULT_CAPACITY = Runtime.getRuntime().availableProcessors() * 100;

    int DEFAULT_KEEP_ALIVE = 60;

    int DEFAULT_BUFFER_SIZE = 512;

    int DEFAULT_SUBSCRIBES = DEFAULT_CPU_THREADS;

}
