package io.effi.rpc.constant;

/**
 * Holds constants for default configuration.
 */
public interface Constant {

    int DEFAULT_IO_THREADS = Math.min(Runtime.getRuntime().availableProcessors() + 1, 32);

    int DEFAULT_CPU_THREADS = Runtime.getRuntime().availableProcessors();

    int DEFAULT_MAX_IO_THREADS = Runtime.getRuntime().availableProcessors() * 5;

    int DEFAULT_MAX_CPU_THREADS = Runtime.getRuntime().availableProcessors() * 2;

    int DEFAULT_CONNECT_TIMEOUT = 6 * 1000;

    long DEFAULT_MAX_CONCURRENT_STREAMS = 1000L;

    int DEFAULT_INITIAL_WINDOW_SIZE = 65535 * 20;

    long DEFAULT_MAX_HEADER_TABLE_SIZE = 4096L;

    int DEFAULT_MAX_HEADER_LIST_SIZE = 8192;

    int DEFAULT_MAX_FRAME_SIZE = 16384;

    int DEFAULT_HEARTBEAT_INTERVAL = 5000;

    int DEFAULT_CAPACITY = Runtime.getRuntime().availableProcessors() * 100;

    int DEFAULT_KEEPALIVE = 60;


    int DEFAULT_BUFFER_SIZE = 512;

    int DEFAULT_SUBSCRIBES = DEFAULT_CPU_THREADS;


    String DEFAULT_VERSION = "1.0.0";

}
