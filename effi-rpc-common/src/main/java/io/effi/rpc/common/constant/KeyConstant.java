package io.effi.rpc.common.constant;

import io.effi.rpc.common.extension.GenericKey;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Holds constants for keys used in the framework.
 */
public interface KeyConstant {

    String UNIQUE_ID = "uniqueId";

    String NAME = "name";

    String PROTOCOL = "protocol";

    String BUFFER_SIZE = "bufferSize";

    String ONEWAY = "oneway";

    String GROUP = "group";

    String VERSION = "version";

    String WEIGHT = "weight";

    String TIMEOUT = "timeout";

    String VERTX = "vertx";

    String PATH = "path";

    String KEEP_ALIVE_TIMEOUT = "keepAliveTimeout";

    String SPARE_CLOSE_TIMES = "spareCloseTimes";

    String HEARTBEAT_LOG_ENABLE = "heartbeatLogEnable";

    String SO_BACKLOG = "soBacklog";

    String REQUEST_CONTEXT = "requestContext";

    String RESPONSE_CONTEXT = "responseContext";

    String ROUTER = "router";

    String TIMESTAMP = "timestamp";

    String URL = "url";

    String PASSWORD = "password";

    String ENABLE_HEALTH_CHECK = "enableHealthCheck";

    String SUBSCRIBES = "subscribes";

    String CA_PATH = "effi-rpc.h2.ca.path";

    String CLIENT_CERT_PATH = "effi-rpc.http.client.cert.path";

    String CLIENT_KEY_PATH = "effi-rpc.http.client.key.path";

    String SERVER_CERT_PATH = "effi-rpc.http.server.cert.path";

    String SERVER_KEY_PATH = "effi-rpc.http.server.key.path";

    GenericKey<Long> ATTR_UNIQUE_ID = GenericKey.valueOf(UNIQUE_ID);

    GenericKey<AtomicInteger> ALL_IDLE_TIMES = GenericKey.valueOf("allIdleTimes");

    GenericKey<AtomicInteger> WRITE_IDLE_TIMES = GenericKey.valueOf("writeIdleTimes");

    GenericKey<AtomicInteger> READER_IDLE_TIMES = GenericKey.valueOf("readeIdleTimes");

    GenericKey<AtomicInteger> LAST_CALL_INDEX = GenericKey.valueOf("lastCallIndex");

}
