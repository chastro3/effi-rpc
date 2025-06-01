package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.config.NodeConfig;
import io.effi.rpc.protocol.http.HttpCaller;
import io.effi.rpc.protocol.http.HttpCallerBuilder;
import io.effi.rpc.protocol.http.support.HttpVersion;
import io.effi.rpc.util.TypeToken;

/**
 * Implements {@link io.effi.rpc.base.Caller} using http1.1.
 */
public class Http1Caller<R> extends HttpCaller<R> {

    Http1Caller(NodeConfig config, Builder<R> builder) {
        super(config, builder);
    }

    public static <R> Builder<R> builder(TypeToken<R> returnType, NodeConfig config) {
        return new Builder<>(returnType, config);
    }

    /**
     * Builds {@link Http1Caller} instance.
     */
    public static class Builder<T> extends HttpCallerBuilder<Http1Caller<T>, Builder<T>> {

        public Builder(TypeToken<T> returnType, NodeConfig config) {
            super(HttpVersion.HTTP_1_1, returnType, config);
        }

        @Override
        protected Http1Caller<T> build(NodeConfig config) {
            return new Http1Caller<>(config, this);
        }

    }
}
