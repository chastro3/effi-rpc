package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.base.Callee;
import io.effi.rpc.base.parameter.MethodMapper;
import io.effi.rpc.config.NodeConfig;
import io.effi.rpc.protocol.http.HttpCallee;
import io.effi.rpc.protocol.http.HttpCalleeBuilder;
import io.effi.rpc.protocol.http.support.HttpVersion;

/**
 * Implements {@link Callee} using http2.
 */
public class Http2Callee extends HttpCallee {

    Http2Callee(NodeConfig config, Builder builder) {
        super(config, builder);
    }

    public static Builder builder(MethodMapper<?> methodMapper, NodeConfig config) {
        return new Builder(methodMapper, config);
    }

    /**
     * Builds {@link Http2Callee} instance.
     */
    public static class Builder extends HttpCalleeBuilder<Http2Callee, Builder> {

        public Builder(MethodMapper<?> methodMapper, NodeConfig config) {
            super(HttpVersion.HTTP_2_0, methodMapper, config);
        }

        @Override
        protected Http2Callee build(NodeConfig config) {
            return new Http2Callee(config, this);
        }

    }
}
