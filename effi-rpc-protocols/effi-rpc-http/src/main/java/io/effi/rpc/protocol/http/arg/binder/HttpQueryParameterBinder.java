package io.effi.rpc.protocol.http.arg.binder;

import io.effi.rpc.context.Peer;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.invocation.Invocation;
import io.effi.rpc.context.parameter.ParameterBinder;
import io.effi.rpc.context.parameter.ParameterBinding;
import io.effi.rpc.protocol.http.HttpInvocationKeys;
import io.effi.rpc.protocol.http.support.HttpRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Binds a query parameter.
 */
public final class HttpQueryParameterBinder implements ParameterBinder {

    private final String name;

    private final String defaultValue;

    public HttpQueryParameterBinder(String name, String defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    @Override
    public void write(Object value, ParameterBinding binding, Invocation invocation) {
        Map<String, String> values = invocation.computeIfAbsent(HttpInvocationKeys.QUERY_PARAMETERS, HashMap::new);
        values.put(name, String.valueOf(value));
    }

    @Override
    public Object resolve(ParameterBinding binding, Request request, Peer peer) {
        return resolveValue(request, name, defaultValue);
    }

    public static Object resolveValue(Request request, String name, String defaultValue) {
        HttpRequest httpRequest = (HttpRequest) request;
        Object value = httpRequest.url().getQueryParam(name);
        return value == null ? defaultValue : value;
    }
}
