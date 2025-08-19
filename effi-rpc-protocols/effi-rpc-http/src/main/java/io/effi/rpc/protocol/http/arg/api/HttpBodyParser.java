package io.effi.rpc.protocol.http.arg.api;

import io.effi.rpc.context.Callee;
import io.effi.rpc.context.parameter.Body;
import io.effi.rpc.context.parameter.ParameterParser;
import io.effi.rpc.protocol.http.support.HttpDuplexRequest;
import io.effi.rpc.protocol.http.support.HttpUtil;
import io.effi.rpc.transport.TransportErrorCodes;

import java.io.IOException;
import java.lang.reflect.Parameter;

/**
 * Parses the body of an HTTP request and decodes it into the appropriate type for the given parameter.
 */
public class HttpBodyParser implements ParameterParser<HttpDuplexRequest> {

    private final Body<?> body;

    public HttpBodyParser(Body<?> body) {
        this.body = body;
    }

    @Override
    public Object parse(HttpDuplexRequest request, Parameter parameter, Callee callee) {
        if (body != null) {
            try {
                return HttpUtil.decodeBody(callee.platform(), request, request.inputStream(), parameter.getParameterizedType());
            } catch (IOException e) {
                throw TransportErrorCodes.DECODE.fail(e, request.getClass(), parameter.getType());
            }
        }
        return null;
    }

    @Override
    public boolean supported(Parameter parameter) {
        return false;
    }
}
