package io.effi.rpc.protocol.http.arg.binder;

import io.effi.rpc.context.invocation.Invocation;
import io.effi.rpc.context.parameter.ParameterBinder;
import io.effi.rpc.context.parameter.ParameterBinding;
import io.effi.rpc.protocol.http.HttpInvocationKeys;
import io.effi.rpc.protocol.http.support.HttpDuplexRequest;
import io.effi.rpc.protocol.http.support.HttpUtil;
import io.effi.rpc.transport.TransportErrorCodes;

import java.io.IOException;

/**
 * Binds the HTTP request body.
 */
public final class HttpBodyParameterBinder implements ParameterBinder {

    public static final HttpBodyParameterBinder INSTANCE = new HttpBodyParameterBinder();

    private HttpBodyParameterBinder() {
    }

    @Override
    public void bind(Object value, ParameterBinding binding, Invocation invocation) {
        invocation.set(HttpInvocationKeys.BODY, value);
    }

    @Override
    public Object resolve(ParameterBinding binding, Invocation invocation) {
        HttpDuplexRequest httpRequest = (HttpDuplexRequest) invocation.get(HttpInvocationKeys.REQUEST);
        try {
            return HttpUtil.decodeBody(
                    invocation.module().platform(),
                    httpRequest,
                    httpRequest.inputStream(),
                    binding.parameter().getParameterizedType()
            );
        } catch (IOException e) {
            throw TransportErrorCodes.DECODE.fail(e, httpRequest.getClass(), binding.parameter().getType());
        }
    }
}
