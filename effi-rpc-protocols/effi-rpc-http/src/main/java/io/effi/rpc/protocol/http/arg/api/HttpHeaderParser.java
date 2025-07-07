package io.effi.rpc.protocol.http.arg.api;

import io.effi.rpc.base.Callee;
import io.effi.rpc.base.parameter.Argument;
import io.effi.rpc.base.parameter.Header;
import io.effi.rpc.base.parameter.ParameterParser;
import io.effi.rpc.protocol.http.support.HttpRequest;
import io.effi.rpc.util.AssertUtil;
import io.netty.buffer.ByteBuf;

import java.lang.reflect.Parameter;

/**
 * Parses the header of an HTTP request and retrieves the value for the specified header key.
 */
public class HttpHeaderParser implements ParameterParser<HttpRequest<ByteBuf>> {

    private final Header<Argument.Source> header;

    public HttpHeaderParser(Header<Argument.Source> header) {
        this.header = AssertUtil.notNull(header, "header");
    }

    @Override
    public Object parse(HttpRequest<ByteBuf> request, Parameter parameter, Callee callee) {
        String key = header.get().get();
        return request.headers().get(key);
    }
}
