package io.effi.rpc.common.constant;

import io.effi.rpc.common.url.Config;
import io.effi.rpc.common.url.ConfigKey;

import static io.effi.rpc.common.url.Config.Source.*;

/**
 * Defines a set of default configuration keys with their sources and default values.
 */
public enum DefaultConfigKeys implements ConfigKey {

    /**
     * The style of annotation used.
     */
    STYLE("style", NULL_OR_FROM_PARENT),

    /**
     * The communication protocol (e.g., HTTP, gRPC).
     */
    PROTOCOL("protocol", NULL_OR_FROM_PARENT),

    /**
     * A list of ports to be excluded.
     */
    EXCLUDED_PORT("excludedPort", MERGE_FROM_PARENT),

    /**
     * The configured path.
     */
    PATH("path", MERGE_FROM_PARENT),

    /**
     * The thread pool configuration.
     */
    THREAD_POOL("threadPool", NULL_OR_FROM_PARENT),

    /**
     * The serialization mechanism (e.g., JSON, Protobuf).
     */
    SERIALIZATION("serialization", NULL_OR_FROM_PARENT),

    /**
     * The compression algorithm to use (e.g., Gzip, LZ4).
     */
    COMPRESSION("compression", NULL_OR_FROM_PARENT),

    /**
     * The threshold for triggering serialization.
     */
    SERIALIZATION_THRESHOLD("serializationThreshold", NULL_OR_FROM_PARENT, "0"),

    /**
     * The threshold for triggering deserialization.
     */
    DESERIALIZATION_THRESHOLD("deserializationThreshold", NULL_OR_FROM_PARENT, "0"),

    /**
     * The list of modules to be used.
     */
    MODULES("modules", MERGE_FROM_PARENT),

    MODULE("module", NULL_OR_FROM_PARENT),

    /**
     * The list of filters to be used.
     */
    FILTERS("filters", MERGE_FROM_PARENT),

    /**
     * The list of registry configurations to be used.
     */
    REGISTRIES("registries", MERGE_FROM_PARENT),

    /**
     * The description of the service.
     */
    DESC("desc", NULL_OR_FROM_PARENT),

    /* -------------------------consumer config----------------------- */

    PROXY("proxy", NULL_OR_FROM_PARENT, Component.ProxyFactory.JDK),

    APPLICATION("application", NULL_OR_FROM_PARENT),

    CLIENT_CONFIG("clientConfig", NULL_OR_FROM_PARENT),

    ADDRESS("address", NULL_OR_FROM_PARENT),

    LOAD_BALANCE("loadBalance", NULL_OR_FROM_PARENT),

    FAULT_TOLERANCE("faultTolerance", NULL_OR_FROM_PARENT),

    RETRIES("retries", NULL_OR_FROM_PARENT),

    TIMEOUT("timeout", NULL_OR_FROM_PARENT),

    SSL("ssl", SELF),
    MAX_CONNECTIONS("maxConnections", SELF),
    MAX_MESSAGE_SIZE("maxMessageSize", SELF),
    CLIENT_MAX_RECEIVE_SIZE("clientMaxReceiveSize", SELF),
    SERVER_MAX_RECEIVE_SIZE("serverMaxReceiveSize", SELF),
    CONNECT_TIMEOUT("connectTimeout", SELF),
    KEEP_ALIVE_TIMEOUT("keepAliveTimeout", SELF),
    SPARE_CLOSE_TIMES("spareCloseTimes", SELF),
    KEEP_ALIVE("keepAlive", SELF),
    MAX_UN_CONNECTIONS("maxUnConnections", SELF),
    MAX_THREADS("maxThreads", SELF),

    /* -------------------------http2 config----------------------- */

    HEADER_TABLE_SIZE("headerTableSize", SELF),
    PUSH_ENABLED("pushEnabled", SELF),
    MAX_CONCURRENT_STREAMS("maxConcurrentStreams", SELF),
    INITIAL_WINDOW_SIZE("initialWindowSize", SELF),
    MAX_FRAME_SIZE("maxFrameSize", SELF),
    MAX_HEADER_LIST_SIZE("maxHeaderListSize", SELF),

    /* -------------------------registry config----------------------- */
    HEALTH_CHECK_INTERVAL("healthCheckInterval", NULL_OR_FROM_PARENT),

    /**
     * The HTTP method (e.g., GET, POST).
     */
    HTTP_METHOD("httpMethod", SELF);

    private final String key;

    private final Config.Source source;

    private final String defaultValue;

    DefaultConfigKeys(String key, Config.Source source) {
        this(key, source, null);
    }

    DefaultConfigKeys(String key, Config.Source source, String defaultValue) {
        this.key = key;
        this.source = source;
        this.defaultValue = defaultValue;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public Config.Source source() {
        return source;
    }

    @Override
    public String defaultValue() {
        return defaultValue;
    }
}

