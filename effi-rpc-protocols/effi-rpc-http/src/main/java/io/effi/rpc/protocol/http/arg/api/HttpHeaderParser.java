package io.effi.rpc.protocol.http.arg.api;

import io.effi.rpc.context.Callee;
import io.effi.rpc.context.parameter.Argument;
import io.effi.rpc.context.parameter.Header;
import io.effi.rpc.context.parameter.ParameterParser;
import io.effi.rpc.protocol.http.support.HttpRequest;
import io.effi.rpc.util.AssertUtil;

import java.lang.reflect.Parameter;

/**
 * Parses the header of an HTTP request and retrieves the value for the specified header key.
 */
public class HttpHeaderParser implements ParameterParser<HttpRequest> {

    private final Header<Argument.Source> header;

    public HttpHeaderParser(Header<Argument.Source> header) {
        this.header = AssertUtil.notNull(header, "header");
    }

    @Override
    public Object parse(HttpRequest request, Parameter parameter, Callee callee) {
        String key = header.get().get();
        return request.headers().get(key);
    }
}
