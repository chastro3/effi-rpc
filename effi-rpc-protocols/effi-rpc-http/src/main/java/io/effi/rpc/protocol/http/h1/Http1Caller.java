package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.protocol.http.HttpCaller;
import io.effi.rpc.util.TypeCapture;

/**
 * Implements {@link io.effi.rpc.context.Caller} using http1.1.
 */
public class Http1Caller<R> extends HttpCaller<R> {

    Http1Caller(Builder<R> builder) {
        super(builder);
    }

    public static <R> Builder<R> builder(TypeCapture<R> returnType) {
        return new Builder<R>(returnType);
    }


    /**
     * Builds {@link Http1Caller} instance.
     */
    public static class Builder<T> extends HttpCaller.Builder<Http1Caller<T>, Builder<T>> {

        public Builder(TypeCapture<T> returnType) {
            super(Http1Protocol.VERSION, returnType);
        }

        @Override
        public Http1Caller<T> build() {
            return new Http1Caller<>(this);
        }

    }
}
