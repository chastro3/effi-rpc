package io.effi.rpc.config;

import io.effi.rpc.constant.Component;
import io.effi.rpc.util.StringUtil;

import static io.effi.rpc.config.ConfigName.Strategy.CASCADED;
import static io.effi.rpc.config.ConfigName.Strategy.SELF_ONLY;
import static io.effi.rpc.config.ConfigName.Strategy.SELF_PREFERRED;
import static io.effi.rpc.constant.Constant.DEFAULT_CLIENT_HYBRID_THREAD_POOL;
import static io.effi.rpc.constant.Constant.DEFAULT_CONNECT_TIMEOUT;
import static io.effi.rpc.constant.Constant.DEFAULT_FAULT_TOLERANCE;
import static io.effi.rpc.constant.Constant.DEFAULT_HEARTBEAT_INTERVAL;
import static io.effi.rpc.constant.Constant.DEFAULT_INITIAL_WINDOW_SIZE;
import static io.effi.rpc.constant.Constant.DEFAULT_MAX_CONCURRENT_STREAMS;
import static io.effi.rpc.constant.Constant.DEFAULT_MAX_CPU_THREADS;
import static io.effi.rpc.constant.Constant.DEFAULT_MAX_FRAME_SIZE;
import static io.effi.rpc.constant.Constant.DEFAULT_MAX_HEADER_LIST_SIZE;
import static io.effi.rpc.constant.Constant.DEFAULT_MAX_HEADER_TABLE_SIZE;
import static io.effi.rpc.constant.Constant.DEFAULT_MAX_MESSAGE_SIZE;
import static io.effi.rpc.constant.Constant.DEFAULT_MAX_UN_CONNECTIONS;
import static io.effi.rpc.constant.Constant.DEFAULT_PROXY;
import static io.effi.rpc.constant.Constant.DEFAULT_RETRIES;
import static io.effi.rpc.constant.Constant.DEFAULT_SERVER_HYBRID_THREAD_POOL;
import static io.effi.rpc.constant.Constant.DEFAULT_TIMEOUT;

/**
 * Defines default config keys with their strategy and default values.
 */
public enum DefaultConfigNames implements ConfigName, ExtensionKeys {

    ANNOTATION_STYLE("style", SELF_PREFERRED),
    PROTOCOL("protocol", SELF_PREFERRED),
    EXCLUDED_PORT("excludedPort", CASCADED, null, ","),
    PATH("path", CASCADED, null, "/"),
    CALLEE_THREAD_POOL("threadPool", SELF_PREFERRED, DEFAULT_SERVER_HYBRID_THREAD_POOL),
    CALLER_THREAD_POOL("threadPool", SELF_PREFERRED, DEFAULT_CLIENT_HYBRID_THREAD_POOL),
    SERIALIZATION("serialization", SELF_PREFERRED),
    COMPRESSION("compression", SELF_PREFERRED),
    SERIALIZATION_THRESHOLD("serializationThreshold", SELF_PREFERRED, 0),
    DESERIALIZATION_THRESHOLD("deserializationThreshold", SELF_PREFERRED, 0),
    MODULE("module", SELF_PREFERRED, Component.DEFAULT),
    CALL_STAGE_CHAIN("callStageChain", SELF_PREFERRED, null, ","),
    REPLY_STAGE_CHAIN("replyStageChain", SELF_PREFERRED, null, ","),
    INTERCEPTOR("interceptor", CASCADED, null, ","),
    INTERCEPTOR_CHAIN("interceptorChain", SELF_PREFERRED, null, ","),
    EXCLUDED_INTERCEPTOR("excludedInterceptor", SELF_ONLY, null, ","),
    REGISTRY("registry", CASCADED, null, ","),
    WEIGHT("weight", SELF_ONLY, 1),
    /* -------------------------caller config----------------------- */
    PROXY("proxy", SELF_PREFERRED, DEFAULT_PROXY),
    REMOTE_APPLICATION("remoteApplication", SELF_PREFERRED),
    REMOTE_MODULE("remoteModule", SELF_PREFERRED),
    CLIENT_CONFIG("clientConfig", SELF_PREFERRED),
    ADDRESS("address", SELF_PREFERRED),
    LOAD_BALANCE("loadBalance", SELF_PREFERRED),
    FAULT_TOLERANCE(ExtensionKeys.FAULT_TOLERANCE, SELF_PREFERRED, DEFAULT_FAULT_TOLERANCE),
    RETRIES("retries", SELF_PREFERRED, DEFAULT_RETRIES),
    TIMEOUT("timeout", SELF_PREFERRED, DEFAULT_TIMEOUT),
    /* -------------------------callee config----------------------- */
    CALLEE_DESC("desc", SELF_PREFERRED),
    /* -------------------------socket config----------------------- */
    SEND_BUFFER_SIZE("sendBufferSize", SELF_ONLY),
    RECEIVE_BUFFER_SIZE("receiveBufferSize", SELF_ONLY),
    /* -------------------------tcp config----------------------- */
    NO_DELAY("noDelay", SELF_ONLY, true),
    KEEP_ALIVE("keepAlive", SELF_ONLY, true),
    SSL("ssl", SELF_ONLY, false),
    /* -------------------------server config----------------------- */
    ACCEPT_BACKLOG("acceptBacklog", SELF_ONLY, DEFAULT_MAX_UN_CONNECTIONS),
    CONNECTION_HANDLER_THREADS("connectHandlers", SELF_ONLY, 1),
    REQUEST_PROCESSOR_THREADS("requestProcessors", SELF_ONLY, DEFAULT_MAX_CPU_THREADS),
    /* -------------------------http config----------------------- */
    TRACING_POLICY("tracingPolicy", SELF_ONLY),
    DECODER_INITIAL_BUFFER_SIZE("decoderInitialBufferSize", SELF_ONLY),
    MAX_MESSAGE_SIZE("maxMessageSize", SELF_ONLY, DEFAULT_MAX_MESSAGE_SIZE),
    /* -------------------------http1.1 config----------------------- */
    MAX_CHUNK_SIZE("maxChunkSize", SELF_ONLY),
    MAX_INITIAL_LINE_LENGTH("maxInitialLineLength", SELF_ONLY),
    MAX_HEADER_SIZE("maxHeaderSize", SELF_ONLY),
    /* -------------------------endpoint config----------------------- */
    MAX_CONNECTIONS("maxConnections", SELF_ONLY, 3),
    CLIENT_MAX_RECEIVE_SIZE("clientMaxReceiveSize", SELF_ONLY, DEFAULT_MAX_MESSAGE_SIZE),
    SERVER_MAX_RECEIVE_SIZE("serverMaxReceiveSize", SELF_ONLY, DEFAULT_MAX_MESSAGE_SIZE),
    CONNECT_TIMEOUT("connectTimeout", SELF_ONLY, DEFAULT_CONNECT_TIMEOUT),
    IDLE_COUNT_THRESHOLD("idleCountThreshold", SELF_ONLY, 6),
    IDLE_TRIGGER_INTERVAL("idleTriggerInterval", SELF_ONLY, 5000),
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
    HEARTBEAT_INTERVAL("heartbeatInterval", SELF_ONLY, DEFAULT_HEARTBEAT_INTERVAL),
    /* -------------------------nacos config----------------------- */
    NACOS_PROJECT_NAME("projectName", SELF_ONLY),
    HTTP_METHOD("httpMethod", SELF_ONLY);

    private final String name;

    private final Strategy strategy;

    private final String defaultValue;

    private final String separator;


    DefaultConfigNames(String name, Strategy strategy) {
        this(name, strategy, null);
    }

    DefaultConfigNames(String name, Strategy strategy, Object defaultValue) {
        this(name, strategy, defaultValue, null);
    }

    DefaultConfigNames(String name, Strategy strategy, Object defaultValue, String separator) {
        this.name = name;
        this.strategy = strategy;
        this.separator = separator == null ? StringUtil.empty() : separator;
        this.defaultValue = defaultValue == null ? null : String.valueOf(defaultValue);
    }

    @Override
    public String realName() {
        return name;
    }

    @Override
    public Strategy strategy() {
        return strategy;
    }

    @Override
    public String defaultValue() {
        return defaultValue;
    }

    @Override
    public String separator() {
        return separator;
    }

}

