package io.effi.rpc.protocol.http;

import io.effi.rpc.config.QueryPath;
import io.effi.rpc.protocol.http.support.HttpRequest;
import io.effi.rpc.util.GenericKey;

import java.util.Map;

/**
 * Defines HTTP-specific invocation attribute keys.
 */
public final class HttpInvocationKeys {

    public static final GenericKey<Map<String, String>> PATH_VARIABLES =
            GenericKey.valueOf("http.pathVariables");

    public static final GenericKey<Map<String, String>> QUERY_PARAMETERS =
            GenericKey.valueOf("http.queryParameters");

    public static final GenericKey<Map<String, String>> HEADERS =
            GenericKey.valueOf("http.headers");

    public static final GenericKey<Object> BODY =
            GenericKey.valueOf("http.body");

    public static final GenericKey<HttpRequest> REQUEST =
            GenericKey.valueOf("http.request");

    public static final GenericKey<QueryPath> PATH_TEMPLATE =
            GenericKey.valueOf("http.pathTemplate");

    private HttpInvocationKeys() {
    }
}
