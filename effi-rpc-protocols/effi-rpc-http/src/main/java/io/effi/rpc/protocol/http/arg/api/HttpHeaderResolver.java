package io.effi.rpc.protocol.http.arg.api;

import io.effi.rpc.context.Servant;
import io.effi.rpc.context.parameter.Argument;
import io.effi.rpc.context.parameter.Header;
import io.effi.rpc.context.parameter.ParameterResolver;
import io.effi.rpc.protocol.http.support.HttpRequest;
import io.effi.rpc.util.AssertUtil;

import java.lang.reflect.Parameter;

/**
 * Parses the header of an HTTP request and retrieves the value for the specified header key.
 */
public class HttpHeaderResolver implements ParameterResolver<HttpRequest> {

    private final Header<Argument.Source> header;

    public HttpHeaderResolver(Header<Argument.Source> header) {
        this.header = AssertUtil.notNull(header, "header");
    }

    @Override
    public Object resolve(HttpRequest request, Parameter parameter, Servant servant) {
        String key = header.get().get();
        return request.headers().get(key);
    }
}
