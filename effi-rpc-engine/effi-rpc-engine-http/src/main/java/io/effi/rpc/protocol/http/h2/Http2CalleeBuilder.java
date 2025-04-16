package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.common.config.NodeConfig;
import io.effi.rpc.contract.parameter.MethodMapper;
import io.effi.rpc.protocol.http.HttpCalleeBuilder;
import io.effi.rpc.protocol.http.h1.Http1Callee;
import io.effi.rpc.protocol.http.support.HttpVersion;

import static io.effi.rpc.common.constant.Component.Protocol.H2;

/**
 * Builder for creating {@link Http1Callee} instances,defining settings for callee.
 *
 * @param <T> The type of service.
 */
public class Http2CalleeBuilder<T> extends HttpCalleeBuilder<Http2Callee<T>, Http2CalleeBuilder<T>> {

    public Http2CalleeBuilder(MethodMapper<?> methodMapper, NodeConfig config) {
        super(methodMapper, config);
        version(HttpVersion.HTTP_2_0);
    }

    @Override
    public String protocol() {
        return H2;
    }

    @Override
    protected Http2Callee<T> build(NodeConfig config) {
        return new Http2Callee<>(config, this);
    }

}
