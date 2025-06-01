package io.effi.rpc.constant;

import io.effi.rpc.util.GenericKey;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Holds constants for keys used in the framework.
 */
public interface KeyConstant {

    String REQUEST_REMOTE_APPLICATION = "effi-rpc-remote-application";

    String REQUEST_REMOTE_MODULE = "effi-rpc-remote-module";

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

    String REQUEST_CONTEXT = "requestContext";

    String RESPONSE_CONTEXT = "responseContext";

    String ROUTER = "router";

    String TIMESTAMP = "timestamp";

    String URL = "url";

    String PASSWORD = "password";

    String ENABLE_HEALTH_CHECK = "enableHealthCheck";

    GenericKey<Long> ATTR_UNIQUE_ID = GenericKey.valueOf(UNIQUE_ID);

    GenericKey<AtomicInteger> IDLE_COUNT = GenericKey.valueOf("idleCount");

    GenericKey<AtomicInteger> LAST_CALL_INDEX = GenericKey.valueOf("lastCallIndex");

    GenericKey<String> SOURCE_PROTOCOL = GenericKey.valueOf("sourceProtocol");

    String TRANSPORTER = "transporter";

}
