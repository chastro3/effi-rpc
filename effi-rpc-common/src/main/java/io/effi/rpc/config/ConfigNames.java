package io.effi.rpc.config;

import static io.effi.rpc.config.ConfigName.Strategy.CURRENT_FIRST;
import static io.effi.rpc.config.ConfigName.Strategy.MERGE_PARENT;
import static io.effi.rpc.config.ConfigName.Strategy.ONLY_CURRENT;
import static io.effi.rpc.config.DefaultConfigName.nameOf;
import static io.effi.rpc.constant.Constant.DEFAULT_CONNECT_TIMEOUT;
import static io.effi.rpc.constant.Constant.DEFAULT_HEARTBEAT_INTERVAL;
import static io.effi.rpc.constant.Constant.DEFAULT_INITIAL_WINDOW_SIZE;
import static io.effi.rpc.constant.Constant.DEFAULT_MAX_CONCURRENT_STREAMS;
import static io.effi.rpc.constant.Constant.DEFAULT_MAX_CPU_THREADS;
import static io.effi.rpc.constant.Constant.DEFAULT_MAX_FRAME_SIZE;
import static io.effi.rpc.constant.Constant.DEFAULT_MAX_HEADER_LIST_SIZE;
import static io.effi.rpc.constant.Constant.DEFAULT_MAX_HEADER_TABLE_SIZE;

/**
 * Provides commonly used ConfigName constants.
 */
public class ConfigNames implements ConfigValues {

    public static final ConfigName<String> ANNOTATION_STYLE = nameOf("style", CURRENT_FIRST);
    public static final ConfigName<String> PROTOCOL = nameOf("protocol", CURRENT_FIRST);
    public static final ConfigName<String[]> SUPPORTED_PROTOCOL = nameOf("protocol", CURRENT_FIRST);
    public static final ConfigName<Integer[]> EXCLUDED_PORT = nameOf("excludedPort", MERGE_PARENT);
    public static final ConfigName<String> PATH = nameOf("path", MERGE_PARENT);
    public static final ConfigName<String> THREAD_POOL = nameOf("threadPool", CURRENT_FIRST, ThreadPool.CLIENT_HYBRID);
    public static final ConfigName<String> CALLEE_THREAD_POOL = nameOf("threadPool", CURRENT_FIRST, ThreadPool.SERVER_HYBRID);
    public static final ConfigName<String> SERIALIZATION = nameOf("serialization", CURRENT_FIRST);
    public static final ConfigName<String> COMPRESSION = nameOf("compression", CURRENT_FIRST, Compression.GZIP);
    public static final ConfigName<Long> SERIALIZATION_THRESHOLD = nameOf("serializationThreshold", CURRENT_FIRST);
    public static final ConfigName<Long> DESERIALIZATION_THRESHOLD = nameOf("deserializationThreshold", CURRENT_FIRST);
    public static final ConfigName<String> MODULE = nameOf("module", CURRENT_FIRST);
    public static final ConfigName<String> THREAD_POOL_CONFIGURATOR = nameOf("threadPoolConfigurator", CURRENT_FIRST);
    public static final ConfigName<String> STAGE_CHAIN_CONFIGURATOR = nameOf("stageChainConfigurator", CURRENT_FIRST);
    public static final ConfigName<String> INTERCEPTOR_CHAIN_CONFIGURATOR = nameOf("interceptorChainConfigurator", CURRENT_FIRST);
    public static final ConfigName<String[]> CALL_STAGE_CHAIN = nameOf("callStageChain", CURRENT_FIRST);
    public static final ConfigName<String[]> REPLY_STAGE_CHAIN = nameOf("replyStageChain", CURRENT_FIRST);
    public static final ConfigName<String[]> INTERCEPTOR = nameOf("interceptor", MERGE_PARENT);
    public static final ConfigName<String[]> INTERCEPTOR_CHAIN = nameOf("interceptorChain", CURRENT_FIRST);
    public static final ConfigName<String[]> EXCLUDED_INTERCEPTOR = nameOf("excludedInterceptor", ONLY_CURRENT);
    public static final ConfigName<String[]> REGISTRY = nameOf("registry", MERGE_PARENT);
    public static final ConfigName<Boolean> IMMUTABLE = nameOf("immutable", ONLY_CURRENT, false, false);
    public static final ConfigName<Integer> WEIGHT = nameOf("weight", ONLY_CURRENT);
    /* -------------------------caller config----------------------- */
    public static final ConfigName<String> TARGET = nameOf("target", CURRENT_FIRST);
    public static final ConfigName<String> LOCATOR = nameOf("locator", CURRENT_FIRST);
    public static final ConfigName<String> PROXY = nameOf("proxy", CURRENT_FIRST, ProxyFactory.JDK);
    public static final ConfigName<String> REMOTE_APPLICATION = nameOf("remoteApplication", CURRENT_FIRST);
    public static final ConfigName<String> REMOTE_MODULE = nameOf("remoteModule", CURRENT_FIRST);
    public static final ConfigName<String> CLIENT_CONFIG = nameOf("clientConfig", CURRENT_FIRST);
    public static final ConfigName<String> ADDRESS = nameOf("address", CURRENT_FIRST);
    public static final ConfigName<String> LOAD_BALANCE = nameOf("loadBalance", CURRENT_FIRST);
    public static final ConfigName<String> FAILURE_HANDLER = nameOf("failureHandler", CURRENT_FIRST);
    public static final ConfigName<Integer> RETRIES = nameOf("retries", CURRENT_FIRST, 3);
    public static final ConfigName<Integer> TIMEOUT = nameOf("timeout", CURRENT_FIRST, 3000);
    /* -------------------------callee config----------------------- */
    public static final ConfigName<String> CALLEE_DESC = nameOf("desc", CURRENT_FIRST);
    /* -------------------------socket config----------------------- */
    public static final ConfigName<Integer> SEND_BUFFER_SIZE = nameOf("sendBufferSize", ONLY_CURRENT);
    public static final ConfigName<Integer> RECEIVE_BUFFER_SIZE = nameOf("receiveBufferSize", ONLY_CURRENT);
    /* -------------------------tcp config----------------------- */
    public static final ConfigName<Boolean> TCP_NO_DELAY = nameOf("tcpNoDelay", ONLY_CURRENT, false, true);
    public static final ConfigName<Boolean> TCP_KEEP_ALIVE = nameOf("keepAlive", ONLY_CURRENT, false, true);
    public static final ConfigName<Boolean> TCP_SSL = nameOf("ssl", ONLY_CURRENT, false, false);
    /* -------------------------server config----------------------- */
    public static final ConfigName<Integer> ACCEPT_BACKLOG = nameOf("acceptBacklog", ONLY_CURRENT, 1024);
    public static final ConfigName<Integer> CONNECTION_HANDLER_THREADS = nameOf("connectHandlers", ONLY_CURRENT, 1);
    public static final ConfigName<Integer> REQUEST_PROCESSOR_THREADS = nameOf("requestProcessors", ONLY_CURRENT, Runtime.getRuntime().availableProcessors() * 2);
    /* -------------------------http config----------------------- */
    public static final ConfigName<String> TRACING_POLICY = nameOf("tracingPolicy", ONLY_CURRENT);
    public static final ConfigName<Integer> DECODER_INITIAL_BUFFER_SIZE = nameOf("decoderInitialBufferSize", ONLY_CURRENT);
    public static final ConfigName<Integer> MAX_MESSAGE_SIZE = nameOf("maxMessageSize", ONLY_CURRENT, 1024 * 32);
    /* -------------------------http1.1 config----------------------- */
    public static final ConfigName<String> HTTP_METHOD = nameOf("httpMethod", ONLY_CURRENT);
    public static final ConfigName<Integer> MAX_CHUNK_SIZE = nameOf("maxChunkSize", ONLY_CURRENT);
    public static final ConfigName<Integer> MAX_INITIAL_LINE_LENGTH = nameOf("maxInitialLineLength", ONLY_CURRENT);
    public static final ConfigName<Integer> MAX_HEADER_SIZE = nameOf("maxHeaderSize", ONLY_CURRENT);
    /* -------------------------endpoint config----------------------- */
    public static final ConfigName<Integer> MAX_CONNECTIONS = nameOf("maxConnections", ONLY_CURRENT, 3);
    public static final ConfigName<Integer> CLIENT_MAX_RECEIVE_SIZE = nameOf("clientMaxReceiveSize", ONLY_CURRENT, MAX_MESSAGE_SIZE.defaultValue());
    public static final ConfigName<Integer> SERVER_MAX_RECEIVE_SIZE = nameOf("serverMaxReceiveSize", ONLY_CURRENT, MAX_MESSAGE_SIZE.defaultValue());
    public static final ConfigName<Integer> CONNECT_TIMEOUT = nameOf("connectTimeout", ONLY_CURRENT, DEFAULT_CONNECT_TIMEOUT);
    public static final ConfigName<Integer> IDLE_COUNT_THRESHOLD = nameOf("idleCountThreshold", ONLY_CURRENT, 6);
    public static final ConfigName<Integer> IDLE_TRIGGER_INTERVAL = nameOf("idleTriggerInterval", ONLY_CURRENT, 5000);
    public static final ConfigName<Integer> MAX_UN_CONNECTIONS = nameOf("maxUnConnections", ONLY_CURRENT, 1024);
    public static final ConfigName<Integer> MAX_THREADS = nameOf("maxThreads", ONLY_CURRENT, DEFAULT_MAX_CPU_THREADS);
    /* -------------------------http2 config----------------------- */
    public static final ConfigName<Long> HEADER_TABLE_SIZE = nameOf("headerTableSize", ONLY_CURRENT, DEFAULT_MAX_HEADER_TABLE_SIZE);
    public static final ConfigName<Boolean> PUSH_ENABLED = nameOf("pushEnabled", ONLY_CURRENT, false, false);
    public static final ConfigName<Long> MAX_CONCURRENT_STREAMS = nameOf("maxConcurrentStreams", ONLY_CURRENT, DEFAULT_MAX_CONCURRENT_STREAMS);
    public static final ConfigName<Integer> INITIAL_WINDOW_SIZE = nameOf("initialWindowSize", ONLY_CURRENT, DEFAULT_INITIAL_WINDOW_SIZE);
    public static final ConfigName<Integer> MAX_FRAME_SIZE = nameOf("maxFrameSize", ONLY_CURRENT, DEFAULT_MAX_FRAME_SIZE);
    public static final ConfigName<Integer> MAX_HEADER_LIST_SIZE = nameOf("maxHeaderListSize", ONLY_CURRENT, DEFAULT_MAX_HEADER_LIST_SIZE);
    /* -------------------------registry config----------------------- */
    public static final ConfigName<Integer> HEARTBEAT_INTERVAL = nameOf("heartbeatInterval", ONLY_CURRENT, DEFAULT_HEARTBEAT_INTERVAL);
    /* -------------------------nacos config---------------------- */
    public static final ConfigName<String> NACOS_PROJECT_NAME = nameOf("projectName", ONLY_CURRENT);

}

