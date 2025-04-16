package io.effi.rpc.common.constant;

import static io.effi.rpc.common.constant.Component.*;

/**
 * Holds constants for default configuration.
 */
public interface Constant {

    String EXTENSION_NAME = "io.effi.rpc.common.spi.Extension";

    String DEFAULT_SERVER_HYBRID_THREAD_POOL = "default-server-hybrid";

    String DEFAULT_CLIENT_HYBRID_THREAD_POOL = "default-server-hybrid";

    int DEFAULT_IO_THREADS = Math.min(Runtime.getRuntime().availableProcessors() + 1, 32);

    int DEFAULT_CPU_THREADS = Runtime.getRuntime().availableProcessors();

    int DEFAULT_MAX_IO_THREADS = Runtime.getRuntime().availableProcessors() * 5;

    int DEFAULT_MAX_CPU_THREADS = Runtime.getRuntime().availableProcessors() * 2;

    int DEFAULT_CONNECT_TIMEOUT = 6 * 1000;

    int DEFAULT_MAX_CONNECT_TIMEOUT = 10 * 1000;

    String UNKNOWN_ADDRESS = "unknown";

    int DEFAULT_TIMEOUT = 4000;

    int DEFAULT_HEART_BEAT_INTERVAL = 6000;

    int DEFAULT_MAX_UN_CONNECTIONS = 1024;

    long DEFAULT_MAX_CONCURRENT_STREAMS = 1000L;

    int DEFAULT_INITIAL_WINDOW_SIZE = 65535 * 20;

    long DEFAULT_MAX_HEADER_TABLE_SIZE = 4096L;

    int DEFAULT_MAX_HEADER_LIST_SIZE = 8192;

    int DEFAULT_MAX_FRAME_SIZE = 16384;

    int DEFAULT_SESSION_TIMEOUT = 60 * 1000;

    int DEFAULT_INTERVAL = 1000;

    int DEFAULT_HEALTH_CHECK_INTERVAL = 5000;

    int DEFAULT_RETRIES = 3;

    int DEFAULT_CAPACITY = Runtime.getRuntime().availableProcessors() * 100;

    int DEFAULT_KEEPALIVE = 60;

    int DEFAULT_PROTOCOL_PORT = 2333;

    int DEFAULT_HTTP_PORT = 9090;

    int DEFAULT_MAX_MESSAGE_SIZE = 1024 * 32;

    String DEFAULT_SERIALIZATION = Serialization.KRYO;

    int DEFAULT_MAX_HEADER_SIZE = 10000;

    int DEFAULT_BUFFER_SIZE = 512;

    int DEFAULT_SUBSCRIBES = DEFAULT_CPU_THREADS;

    int DEFAULT_IDLE_COUNT_THRESHOLD = 3;

    int DEFAULT_CLIENT_MAX_CONNECTIONS = 3;

    String DEFAULT_VERSION = "1.0.0";

    String DEFAULT_GROUP = DEFAULT;

    int DEFAULT_WEIGHT = 0;

    String DEFAULT_ROUTER = DEFAULT;

    String DEFAULT_SERVICE_DISCOVERY = DEFAULT;

    String DEFAULT_PROXY = ProxyFactory.JDK;

    String DEFAULT_REGISTRY = Registry.CONSUL;

    String DEFAULT_FAULT_TOLERANCE = FaultTolerance.FAIL_FAST;

    String DEFAULT_LOAD_BALANCE = LoadBalance.ROUND_ROBIN;

    String DEFAULT_TRANSPORTER = Transport.NETTY;

    String SPI_FIX_PATH = "META-INF/effi-rpc/services/";

    String NATIVE_IMAGE_PREFIX = "META-INF/native-image/";

    String INTERNAL_CERTS_PATH = "META-INF/effi-rpc/internal/certs/";

    String DEFAULT_COMPRESSION = Compression.GZIP;

    long DEFAULT_SERIALIZATION_THRESHOLD = 0;

    long DEFAULT_DESERIALIZATION_THRESHOLD = 0;
}
