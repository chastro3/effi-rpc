package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.config.HierarchicalConfig;
import io.effi.rpc.context.Callee;
import io.effi.rpc.context.parameter.MethodMapper;
import io.effi.rpc.protocol.http.HttpCallee;

/**
 * Implements {@link Callee} using http2.
 */
public class Http2Callee extends HttpCallee {

    Http2Callee(Builder builder) {
        super(builder);
    }

    public static Builder builder(MethodMapper<?> methodMapper) {
        return new Builder(methodMapper, null);
    }

    public static Builder builder(MethodMapper<?> methodMapper, HierarchicalConfig config) {
        return new Builder(methodMapper, config);
    }

    /**
     * Builds {@link Http2Callee} instance.
     */
    public static class Builder extends HttpCallee.Builder<Http2Callee, Builder> {

        public Builder(MethodMapper<?> methodMapper, HierarchicalConfig config) {
            super(Http2Protocol.VERSION, methodMapper, config);
        }

        @Override
        public Http2Callee build() {
            return new Http2Callee(this);
        }

    }
}
