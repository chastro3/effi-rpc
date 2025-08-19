package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.config.HierarchicalConfig;
import io.effi.rpc.protocol.http.HttpCaller;
import io.effi.rpc.util.TypeCapture;

/**
 * Implements {@link io.effi.rpc.context.Caller} using http2.
 */
public class Http2Caller<R> extends HttpCaller<R> {

    protected Http2Caller(Builder<R> builder) {
        super(builder);
    }

    public static <R> Builder<R> builder(TypeCapture<R> replyType) {
        return new Builder<>(replyType, null);
    }

    public static <R> Builder<R> builder(TypeCapture<R> returnType, HierarchicalConfig config) {
        return new Builder<>(returnType, config);
    }

    /**
     * Builds {@link Http2Caller} instance.
     */
    public static class Builder<T> extends HttpCaller.Builder<Http2Caller<T>, Builder<T>> {

        public Builder(TypeCapture<T> returnType, HierarchicalConfig config) {
            super(Http2Protocol.VERSION, returnType, config);
        }

        @Override
        public Http2Caller<T> build() {
            return new Http2Caller<>(this);
        }
    }
}
