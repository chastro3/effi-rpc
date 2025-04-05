package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.common.config.LinkedConfig;
import io.effi.rpc.contract.parameter.MethodMapper;
import io.effi.rpc.protocol.http.HttpCalleeBuilder;

import static io.effi.rpc.common.constant.Component.Protocol.HTTP;

/**
 * Builder for creating {@link Http1Callee} instances,defining settings for callee.
 *
 * @param <T> The type of service.
 */
public class Http1CalleeBuilder<T> extends HttpCalleeBuilder<Http1Callee<T>, Http1CalleeBuilder<T>> {

    public Http1CalleeBuilder(MethodMapper<?> methodMapper, LinkedConfig config) {
        super(methodMapper, config);
    }

    @Override
    public String protocol() {
        return HTTP;
    }

    @Override
    protected Http1Callee<T> build(LinkedConfig config) {
        return new Http1Callee<>(config, this);
    }

}
