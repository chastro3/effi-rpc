package io.effi.rpc.common.config;

import static io.effi.rpc.common.config.ConfigKey.Strategy.*;
import static io.effi.rpc.common.constant.Constant.*;

/**
 * Defines a set of default configuration keys with their sources and default values.
 */
public enum DefaultConfigKeys implements ConfigKey {

    ANNOTATION_STYLE("style", SELF_PREFERRED),
    PROTOCOL("protocol", SELF_PREFERRED),
    EXCLUDED_PORT("excludedPort", CASCADED),
    PATH("path", CASCADED),
    THREAD_POOL("threadPool", SELF_PREFERRED),
    SERIALIZATION("serialization", SELF_PREFERRED),
    COMPRESSION("compression", SELF_PREFERRED),
    SERIALIZATION_THRESHOLD("serializationThreshold", SELF_PREFERRED, 0),
    DESERIALIZATION_THRESHOLD("deserializationThreshold", SELF_PREFERRED, 0),
    MODULES("modules", CASCADED),
    MODULE("module", SELF_PREFERRED),
    FILTERS("filters", CASCADED),
    REGISTRIES("registries", CASCADED),
    CALLEE_DESC("desc", SELF_PREFERRED),

    /* -------------------------caller config----------------------- */
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
    MAX_CONNECTIONS("maxConnections", SELF_ONLY, 3),
    MAX_MESSAGE_SIZE("maxMessageSize", SELF_ONLY),
    CLIENT_MAX_RECEIVE_SIZE("clientMaxReceiveSize", SELF_ONLY),
    SERVER_MAX_RECEIVE_SIZE("serverMaxReceiveSize", SELF_ONLY),
    CONNECT_TIMEOUT("connectTimeout", SELF_ONLY),
    IDLE_COUNT_THRESHOLD("idleCountThreshold", SELF_ONLY, 6),
    IDLE_TRIGGER_INTERVAL("idleTriggerInterval", SELF_ONLY, 5000),
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

    HTTP_METHOD("httpMethod", SELF_ONLY);

    private final String key;

    private final Strategy strategy;

    private final String defaultValue;

    DefaultConfigKeys(String key, Strategy strategy) {
        this(key, strategy, null);
    }

    DefaultConfigKeys(String key, Strategy strategy, Object defaultValue) {
        this.key = key;
        this.strategy = strategy;
        this.defaultValue = defaultValue == null ? null : String.valueOf(defaultValue);
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public Strategy strategy() {
        return strategy;
    }

    @Override
    public String defaultValue() {
        return defaultValue;
    }
}

