package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.config.NodeConfig;
import io.effi.rpc.protocol.http.HttpCaller;
import io.effi.rpc.protocol.http.HttpCallerBuilder;
import io.effi.rpc.protocol.http.support.HttpVersion;
import io.effi.rpc.util.TypeToken;

/**
 * Implements {@link io.effi.rpc.base.Caller} using http2.
 */
public class Http2Caller<R> extends HttpCaller<R> {

    protected Http2Caller(NodeConfig config, Builder<R> builder) {
        super(config, builder);
    }

    public static <R> Builder<R> builder(TypeToken<R> returnType, NodeConfig config) {
        return new Builder<>(returnType, config);
    }

    /**
     * Builds {@link Http2Caller} instance.
     */
    public static class Builder<T> extends HttpCallerBuilder<Http2Caller<T>, Builder<T>> {

        public Builder(TypeToken<T> returnType, NodeConfig config) {
            super(HttpVersion.HTTP_2_0, returnType, config);
        }

        @Override
        protected Http2Caller<T> build(NodeConfig config) {
            return new Http2Caller<>(config, this);
        }
    }
}
