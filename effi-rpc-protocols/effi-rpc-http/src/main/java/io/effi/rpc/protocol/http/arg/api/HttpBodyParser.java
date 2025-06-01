package io.effi.rpc.protocol.http.arg.api;

import io.effi.rpc.base.Callee;
import io.effi.rpc.base.parameter.Body;
import io.effi.rpc.base.parameter.ParameterParser;
import io.effi.rpc.protocol.http.support.HttpRequest;
import io.effi.rpc.protocol.http.support.HttpUtil;
import io.netty.buffer.ByteBuf;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

/**
 * Parses the body of an HTTP request and decodes it into the appropriate type for the given parameter.
 */
public class HttpBodyParser implements ParameterParser<HttpRequest<ByteBuf>> {

    private final Body<?> body;

    public HttpBodyParser(Body<?> body) {
        this.body = body;
    }

    @Override
    public Object parse(HttpRequest<ByteBuf> request, Parameter parameter, Callee<?> callee) {
        if (body != null && !request.isInstance()) {
            Type type = parameter.getParameterizedType();
            return HttpUtil.decodeBody(request, type);
        }
        return null;
    }

    @Override
    public boolean supported(Parameter parameter) {
        return false;
    }
}
