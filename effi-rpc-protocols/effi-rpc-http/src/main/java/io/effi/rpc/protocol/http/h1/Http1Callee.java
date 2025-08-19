package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.config.HierarchicalConfig;
import io.effi.rpc.context.Callee;
import io.effi.rpc.context.parameter.MethodMapper;
import io.effi.rpc.protocol.http.HttpCallee;

/**
 * Implements {@link Callee} using http1.1.
 */
public class Http1Callee extends HttpCallee {

    Http1Callee(Builder builder) {
        super(builder);
    }

    public static Builder builder(MethodMapper<?> methodMapper) {
        return new Builder(methodMapper, null);
    }

    public static Builder builder(MethodMapper<?> methodMapper, HierarchicalConfig config) {
        return new Builder(methodMapper, config);
    }

    /**
     * Builds {@link Http1Callee} instance.
     */
    public static class Builder extends HttpCallee.Builder<Http1Callee, Builder> {

        public Builder(MethodMapper<?> methodMapper, HierarchicalConfig config) {
            super(Http1Protocol.VERSION, methodMapper, config);
        }

        @Override
        public Http1Callee build() {
            return new Http1Callee(this);
        }
    }

}
