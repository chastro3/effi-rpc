package io.effi.rpc.protocol.http.arg.api;

import io.effi.rpc.context.Servant;
import io.effi.rpc.context.parameter.Body;
import io.effi.rpc.context.parameter.ParameterResolver;
import io.effi.rpc.protocol.http.support.HttpDuplexRequest;
import io.effi.rpc.protocol.http.support.HttpUtil;
import io.effi.rpc.transport.TransportErrorCodes;

import java.io.IOException;
import java.lang.reflect.Parameter;

/**
 * Parses the body of an HTTP request and decodes it into the appropriate type for the given parameter.
 */
public class HttpBodyResolver implements ParameterResolver<HttpDuplexRequest> {

    private final Body<?> body;

    public HttpBodyResolver(Body<?> body) {
        this.body = body;
    }

    @Override
    public Object resolve(HttpDuplexRequest request, Parameter parameter, Servant servant) {
        if (body != null) {
            try {
                return HttpUtil.decodeBody(servant.platform(), request, request.inputStream(), parameter.getParameterizedType());
            } catch (IOException e) {
                throw TransportErrorCodes.DECODE.fail(e, request.getClass(), parameter.getType());
            }
        }
        return null;
    }

    @Override
    public boolean supports(Parameter parameter) {
        return false;
    }
}
