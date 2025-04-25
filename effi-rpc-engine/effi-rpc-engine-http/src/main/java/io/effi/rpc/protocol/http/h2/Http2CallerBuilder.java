package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.config.NodeConfig;
import io.effi.rpc.util.TypeToken;
import io.effi.rpc.contract.config.ClientConfig;
import io.effi.rpc.protocol.http.HttpCallerBuilder;
import io.effi.rpc.protocol.http.support.HttpVersion;

import static io.effi.rpc.constant.Component.Protocol.H2;

/**
 * Builder for creating {@link Http2Caller} instances,defining settings for caller.
 *
 * @param <T> The type of return.
 */

public class Http2CallerBuilder<T> extends HttpCallerBuilder<Http2Caller<T>, Http2CallerBuilder<T>> {

    public Http2CallerBuilder(TypeToken<T> returnType, NodeConfig config) {
        super(returnType, config);
        version(HttpVersion.HTTP_2_0);
    }

    @Override
    public String protocol() {
        return H2;
    }

    @Override
    protected Http2Caller<T> build(NodeConfig config) {
        return new Http2Caller<>(config, this);
    }

    @Override
    protected ClientConfig defaultConfig() {
        return Http2ClientConfig.defaultConfig();
    }
}
