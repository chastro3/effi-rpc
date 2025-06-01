package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.base.Callee;
import io.effi.rpc.base.parameter.MethodMapper;
import io.effi.rpc.config.NodeConfig;
import io.effi.rpc.protocol.http.HttpCallee;
import io.effi.rpc.protocol.http.HttpCalleeBuilder;
import io.effi.rpc.protocol.http.support.HttpVersion;

/**
 * Implements {@link Callee} using http1.1.
 */
public class Http1Callee<T> extends HttpCallee<T> {

    Http1Callee(NodeConfig config, Builder<T> builder) {
        super(config, builder);
    }

    public static <T> Builder<T> builder(MethodMapper<T> methodMapper, NodeConfig config) {
        return new Builder<>(methodMapper, config);
    }

    /**
     * Builds {@link Http1Callee} instance.
     */
    public static class Builder<T> extends HttpCalleeBuilder<Http1Callee<T>, Builder<T>> {

        public Builder(MethodMapper<?> methodMapper, NodeConfig config) {
            super(HttpVersion.HTTP_1_1, methodMapper, config);
        }

        @Override
        protected Http1Callee<T> build(NodeConfig config) {
            return new Http1Callee<>(config, this);
        }

    }

}
