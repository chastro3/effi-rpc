package io.effi.rpc.common.config;

import static io.effi.rpc.common.config.LinkedConfig.Source.*;
import static io.effi.rpc.common.constant.Constant.*;

/**
 * Defines a set of default configuration keys with their sources and default values.
 */
public enum DefaultConfigKeys implements ConfigKey {

    /**
     * The style of annotation used.
     */
    STYLE("style", SELF_PREFERRED),

    /**
     * The communication protocol (e.g., HTTP, gRPC).
     */
    PROTOCOL("protocol", SELF_PREFERRED),

    /**
     * A list of ports to be excluded.
     */
    EXCLUDED_PORT("excludedPort", CASCADED),

    /**
     * The configured path.
     */
    PATH("path", CASCADED),

    /**
     * The thread pool configuration.
     */
    THREAD_POOL("threadPool", SELF_PREFERRED),

    /**
     * The serialization mechanism (e.g., JSON, Protobuf).
     */
    SERIALIZATION("serialization", SELF_PREFERRED),

    /**
     * The compression algorithm to use (e.g., Gzip, LZ4).
     */
    COMPRESSION("compression", SELF_PREFERRED),

    /**
     * The threshold for triggering serialization.
     */
    SERIALIZATION_THRESHOLD("serializationThreshold", SELF_PREFERRED, 0),

    /**
     * The threshold for triggering deserialization.
     */
    DESERIALIZATION_THRESHOLD("deserializationThreshold", SELF_PREFERRED, 0),

    /**
     * The list of modules to be used.
     */
    MODULES("modules", CASCADED),

    MODULE("module", SELF_PREFERRED),

    /**
     * The list of filters to be used.
     */
    FILTERS("filters", CASCADED),

    /**
     * The list of registry configurations to be used.
     */
    REGISTRIES("registries", CASCADED),

    /**
     * The description of the service.
     */
    CALLEE_DESC("desc", SELF_PREFERRED),

    /* -------------------------consumer config----------------------- */

    PROXY("proxy", SELF_PREFERRED, DEFAULT_PROXY),
    APPLICATION("application", SELF_PREFERRED),
    CLIENT_CONFIG("clientConfig", SELF_PREFERRED),
    ADDRESS("address", SELF_PREFERRED),
    LOAD_BALANCE("loadBalance", SELF_PREFERRED),
    FAULT_TOLERANCE("faultTolerance", SELF_PREFERRED, DEFAULT_FAULT_TOLERANCE),
    RETRIES("retries", SELF_PREFERRED, DEFAULT_RETRIES),
    TIMEOUT("timeout", SELF_PREFERRED, DEFAULT_TIMEOUT),

    /* -------------------------endpoint config----------------------- */
    SSL("ssl", SELF_ONLY),
    MAX_CONNECTIONS("maxConnections", SELF_ONLY),
    MAX_MESSAGE_SIZE("maxMessageSize", SELF_ONLY),
    CLIENT_MAX_RECEIVE_SIZE("clientMaxReceiveSize", SELF_ONLY),
    SERVER_MAX_RECEIVE_SIZE("serverMaxReceiveSize", SELF_ONLY),
    CONNECT_TIMEOUT("connectTimeout", SELF_ONLY),
    IDLE_COUNT_THRESHOLD("idleCountThreshold", SELF_ONLY, 6),
    IDLE_TRIGGER_INTERVAL("idleTriggerInterval", SELF_ONLY, 5),
    KEEP_ALIVE("keepAlive", SELF_ONLY, true),
    MAX_UN_CONNECTIONS("maxUnConnections", SELF_ONLY, DEFAULT_MAX_UN_CONNECTIONS),
    MAX_THREADS("maxThreads", SELF_ONLY, DEFAULT_MAX_CPU_THREADS),

    /* -------------------------http2 config----------------------- */

    HEADER_TABLE_SIZE("headerTableSize", SELF_ONLY, DEFAULT_MAX_HEADER_TABLE_SIZE),
    PUSH_ENABLED("pushEnabled", SELF_ONLY, false),
    MAX_CONCURRENT_STREAMS("maxConcurrentStreams", SELF_ONLY, DEFAULT_MAX_CONCURRENT_STREAMS),
    INITIAL_WINDOW_SIZE("initialWindowSize", SELF_ONLY, DEFAULT_INITIAL_WINDOW_SIZE),
    MAX_FRAME_SIZE("maxFrameSize", SELF_ONLY, DEFAULT_MAX_FRAME_SIZE),
    MAX_HEADER_LIST_SIZE("maxHeaderListSize", SELF_ONLY, DEFAULT_MAX_HEADER_LIST_SIZE),

    /* -------------------------registry config----------------------- */
    HEALTH_CHECK_INTERVAL("healthCheckInterval", SELF_ONLY, DEFAULT_HEALTH_CHECK_INTERVAL),

    /**
     * The HTTP method (e.g., GET, POST).
     */
    HTTP_METHOD("httpMethod", SELF_ONLY);

    private final String key;

    private final LinkedConfig.Source source;

    private final String defaultValue;

    DefaultConfigKeys(String key, LinkedConfig.Source source) {
        this(key, source, null);
    }

    DefaultConfigKeys(String key, LinkedConfig.Source source, Object defaultValue) {
        this.key = key;
        this.source = source;
        this.defaultValue = defaultValue == null ? null : String.valueOf(defaultValue);
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public LinkedConfig.Source source() {
        return source;
    }

    @Override
    public String defaultValue() {
        return defaultValue;
    }
}

