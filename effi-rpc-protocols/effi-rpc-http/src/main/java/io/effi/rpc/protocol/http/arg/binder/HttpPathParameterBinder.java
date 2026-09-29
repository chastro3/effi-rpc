package io.effi.rpc.protocol.http.arg.binder;

import io.effi.rpc.config.QueryPath;
import io.effi.rpc.context.invocation.Invocation;
import io.effi.rpc.context.parameter.ParameterBinder;
import io.effi.rpc.context.parameter.ParameterBinding;
import io.effi.rpc.protocol.http.HttpInvocationKeys;
import io.effi.rpc.protocol.http.support.HttpRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Binds a path variable.
 */
public final class HttpPathParameterBinder implements ParameterBinder {

    private final String name;

    private final String defaultValue;

    public HttpPathParameterBinder(String name, String defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    @Override
    public void bind(Object value, ParameterBinding binding, Invocation invocation) {
        Map<String, String> values = invocation.computeIfAbsent(HttpInvocationKeys.PATH_VARIABLES, HashMap::new);
        values.put(name, String.valueOf(value));
    }

    @Override
    public Object resolve(ParameterBinding binding, Invocation invocation) {
        return resolveValue(invocation, name, defaultValue);
    }

    public static Object resolveValue(Invocation invocation, String name, String defaultValue) {
        HttpRequest request = invocation.get(HttpInvocationKeys.REQUEST);
        QueryPath pathTemplate = invocation.get(HttpInvocationKeys.PATH_TEMPLATE);
        Map<String, String> values = pathTemplate.match(request.url().path());
        Object value = values == null ? null : values.get(name);
        return value == null ? defaultValue : value;
    }
}
