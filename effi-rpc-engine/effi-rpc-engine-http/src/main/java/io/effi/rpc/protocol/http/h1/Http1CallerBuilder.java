package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.common.config.NodeConfig;
import io.effi.rpc.common.util.TypeToken;
import io.effi.rpc.contract.config.ClientConfig;
import io.effi.rpc.protocol.http.HttpCallerBuilder;

import static io.effi.rpc.common.constant.Component.Protocol.HTTP;

/**
 * Builder for creating {@link Http1Caller} instances,defining settings for caller.
 *
 * @param <T> The type of return.
 */
public class Http1CallerBuilder<T> extends HttpCallerBuilder<Http1Caller<T>, Http1CallerBuilder<T>> {

    public Http1CallerBuilder(TypeToken<T> returnType, NodeConfig config) {
        super(returnType, config);
    }

    @Override
    public String protocol() {
        return HTTP;
    }

    @Override
    protected Http1Caller<T> build(NodeConfig config) {
        return new Http1Caller<>(config, this);
    }

    @Override
    protected ClientConfig defaultConfig() {
        return null;
    }
}
