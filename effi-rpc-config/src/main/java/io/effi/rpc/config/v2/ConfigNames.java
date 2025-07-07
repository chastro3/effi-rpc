package io.effi.rpc.config.v2;


import io.effi.rpc.executor.RpcThreadPool;

import java.util.concurrent.ExecutorService;

import static io.effi.rpc.config.v2.ConfigName.Strategy.CASCADED;
import static io.effi.rpc.config.v2.ConfigName.Strategy.SELF_ONLY;
import static io.effi.rpc.config.v2.ConfigName.Strategy.SELF_PREFERRED;
import static io.effi.rpc.config.v2.DefaultConfigName.nameOf;
import static io.effi.rpc.constant.Constant.DEFAULT_CONNECT_TIMEOUT;
import static io.effi.rpc.constant.Constant.DEFAULT_HEARTBEAT_INTERVAL;
import static io.effi.rpc.constant.Constant.DEFAULT_INITIAL_WINDOW_SIZE;
import static io.effi.rpc.constant.Constant.DEFAULT_MAX_CONCURRENT_STREAMS;
import static io.effi.rpc.constant.Constant.DEFAULT_MAX_CPU_THREADS;
import static io.effi.rpc.constant.Constant.DEFAULT_MAX_FRAME_SIZE;
import static io.effi.rpc.constant.Constant.DEFAULT_MAX_HEADER_LIST_SIZE;
import static io.effi.rpc.constant.Constant.DEFAULT_MAX_HEADER_TABLE_SIZE;

public class ConfigNames implements ConfigValues {

    public static final ConfigName<String> ANNOTATION_STYLE = nameOf("style", SELF_PREFERRED);
    public static final ConfigName<String> PROTOCOL = nameOf("protocol", SELF_PREFERRED);
    public static final ConfigName<Integer[]> EXCLUDED_PORT = nameOf("excludedPort", CASCADED);
    public static final ConfigName<String[]> PATH = nameOf("path", CASCADED);
    public static final ConfigName<ExecutorService> CALLER_THREAD_POOL = nameOf("threadPool", SELF_PREFERRED,
            () -> RpcThreadPool.defaultCPUExecutor(ThreadPool.CLIENT_HYBRID)
    );
    public static final ConfigName<ExecutorService> CALLEE_THREAD_POOL = nameOf("threadPool", SELF_PREFERRED,
            () -> RpcThreadPool.defaultIOExecutor(ThreadPool.SERVER_HYBRID));
    public static final ConfigName<String> SERIALIZATION = nameOf("serialization", SELF_PREFERRED);
    public static final ConfigName<String> COMPRESSION = nameOf("compression", SELF_PREFERRED, Compression.GZIP);
    public static final ConfigName<Integer> SERIALIZATION_THRESHOLD = nameOf("serializationThreshold", SELF_PREFERRED);
    public static final ConfigName<Integer> DESERIALIZATION_THRESHOLD = nameOf("deserializationThreshold", SELF_PREFERRED);
    public static final ConfigName<String> MODULE = nameOf("module", SELF_PREFERRED);
    public static final ConfigName<String[]> CALL_STAGE_CHAIN = nameOf("callStageChain", SELF_PREFERRED);
    public static final ConfigName<String[]> REPLY_STAGE_CHAIN = nameOf("replyStageChain", SELF_PREFERRED);
    public static final ConfigName<String[]> INTERCEPTOR = nameOf("interceptor", CASCADED);
    public static final ConfigName<String[]> INTERCEPTOR_CHAIN = nameOf("interceptorChain", SELF_PREFERRED);
    public static final ConfigName<String[]> EXCLUDED_INTERCEPTOR = nameOf("excludedInterceptor", SELF_ONLY);
    public static final ConfigName<String[]> REGISTRY = nameOf("registry", CASCADED);
    public static final ConfigName<String> WEIGHT = nameOf("weight", SELF_ONLY);
    /* -------------------------caller config----------------------- */
    public static final ConfigName<String> PROXY = nameOf("proxy", SELF_PREFERRED, ProxyFactory.JDK);
    public static final ConfigName<String> REMOTE_APPLICATION = nameOf("remoteApplication", SELF_PREFERRED);
    public static final ConfigName<String> REMOTE_MODULE = nameOf("remoteModule", SELF_PREFERRED);
    public static final ConfigName<String> CLIENT_CONFIG = nameOf("clientConfig", SELF_PREFERRED);
    public static final ConfigName<String> ADDRESS = nameOf("address", SELF_PREFERRED);
    public static final ConfigName<String> LOAD_BALANCE = nameOf("loadBalance", SELF_PREFERRED);
    public static final ConfigName<String> FAULT_TOLERANCE = nameOf("faultTolerance", SELF_PREFERRED, FaultTolerance.FAIL_FAST);
    public static final ConfigName<Integer> RETRIES = nameOf("retries", SELF_PREFERRED, 3);
    public static final ConfigName<Integer> TIMEOUT = nameOf("timeout", SELF_PREFERRED, 3000);
    /* -------------------------callee config----------------------- */
    public static final ConfigName<String> CALLEE_DESC = nameOf("desc", SELF_PREFERRED);
    /* -------------------------socket config----------------------- */
    public static final ConfigName<Integer> SEND_BUFFER_SIZE = nameOf("sendBufferSize", SELF_ONLY);
    public static final ConfigName<Integer> RECEIVE_BUFFER_SIZE = nameOf("receiveBufferSize", SELF_ONLY);
    /* -------------------------tcp config----------------------- */
    public static final ConfigName<Boolean> TCP_NO_DELAY = nameOf("tcpNoDelay", SELF_ONLY, true);
    public static final ConfigName<Boolean> TCP_KEEP_ALIVE = nameOf("keepAlive", SELF_ONLY, true);
    public static final ConfigName<Boolean> TCP_SSL = nameOf("ssl", SELF_ONLY, false);
    /* -------------------------server config----------------------- */
    public static final ConfigName<Integer> ACCEPT_BACKLOG = nameOf("acceptBacklog", SELF_ONLY, 1024);
    public static final ConfigName<Integer> CONNECTION_HANDLER_THREADS = nameOf("connectHandlers", SELF_ONLY, 1);
    public static final ConfigName<Integer> REQUEST_PROCESSOR_THREADS = nameOf("requestProcessors", SELF_ONLY, Runtime.getRuntime().availableProcessors() * 2);
    /* -------------------------http config----------------------- */
    public static final ConfigName<Integer> TRACING_POLICY = nameOf("tracingPolicy", SELF_ONLY);
    public static final ConfigName<Integer> DECODER_INITIAL_BUFFER_SIZE = nameOf("decoderInitialBufferSize", SELF_ONLY);
    public static final ConfigName<Integer> MAX_MESSAGE_SIZE = nameOf("maxMessageSize", SELF_ONLY, 1024 * 32);
    /* -------------------------http1.1 config----------------------- */
    public static final ConfigName<String> HTTP_METHOD = nameOf("httpMethod", SELF_ONLY);
    public static final ConfigName<Integer> MAX_CHUNK_SIZE = nameOf("maxChunkSize", SELF_ONLY);
    public static final ConfigName<Integer> MAX_INITIAL_LINE_LENGTH = nameOf("maxInitialLineLength", SELF_ONLY);
    public static final ConfigName<Integer> MAX_HEADER_SIZE = nameOf("maxHeaderSize", SELF_ONLY);
    /* -------------------------endpoint config----------------------- */
    public static final ConfigName<Integer> MAX_CONNECTIONS = nameOf("maxConnections", SELF_ONLY, 3);
    public static final ConfigName<Integer> CLIENT_MAX_RECEIVE_SIZE = nameOf("clientMaxReceiveSize", SELF_ONLY, MAX_MESSAGE_SIZE.defaultValue());
    public static final ConfigName<Integer> SERVER_MAX_RECEIVE_SIZE = nameOf("serverMaxReceiveSize", SELF_ONLY, MAX_MESSAGE_SIZE.defaultValue());
    public static final ConfigName<Integer> CONNECT_TIMEOUT = nameOf("connectTimeout", SELF_ONLY, DEFAULT_CONNECT_TIMEOUT);
    public static final ConfigName<Integer> IDLE_COUNT_THRESHOLD = nameOf("idleCountThreshold", SELF_ONLY, 6);
    public static final ConfigName<Integer> IDLE_TRIGGER_INTERVAL = nameOf("idleTriggerInterval", SELF_ONLY, 5000);
    public static final ConfigName<Integer> MAX_UN_CONNECTIONS = nameOf("maxUnConnections", SELF_ONLY, 1024);
    public static final ConfigName<Integer> MAX_THREADS = nameOf("maxThreads", SELF_ONLY, DEFAULT_MAX_CPU_THREADS);
    /* -------------------------http2 config----------------------- */
    public static final ConfigName<Long> HEADER_TABLE_SIZE = nameOf("headerTableSize", SELF_ONLY, DEFAULT_MAX_HEADER_TABLE_SIZE);
    public static final ConfigName<Boolean> PUSH_ENABLED = nameOf("pushEnabled", SELF_ONLY, false);
    public static final ConfigName<Long> MAX_CONCURRENT_STREAMS = nameOf("maxConcurrentStreams", SELF_ONLY, DEFAULT_MAX_CONCURRENT_STREAMS);
    public static final ConfigName<Integer> INITIAL_WINDOW_SIZE = nameOf("initialWindowSize", SELF_ONLY, DEFAULT_INITIAL_WINDOW_SIZE);
    public static final ConfigName<Integer> MAX_FRAME_SIZE = nameOf("maxFrameSize", SELF_ONLY, DEFAULT_MAX_FRAME_SIZE);
    public static final ConfigName<Integer> MAX_HEADER_LIST_SIZE = nameOf("maxHeaderListSize", SELF_ONLY, DEFAULT_MAX_HEADER_LIST_SIZE);
    /* -------------------------registry config----------------------- */
    public static final ConfigName<Integer> HEARTBEAT_INTERVAL = nameOf("heartbeatInterval", SELF_ONLY, DEFAULT_HEARTBEAT_INTERVAL);
    /* -------------------------nacos config---------------------- */
    public static final ConfigName<String> NACOS_PROJECT_NAME = nameOf("projectName", SELF_ONLY);

}

