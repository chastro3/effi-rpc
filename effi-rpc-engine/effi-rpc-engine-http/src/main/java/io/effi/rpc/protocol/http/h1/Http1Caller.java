package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.common.config.LinkedConfig;
import io.effi.rpc.common.util.TypeToken;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.protocol.http.HttpCaller;

/**
 * Http1.1 protocol implementation of {@link Caller}.
 *
 * @param <R>
 */
public class Http1Caller<R> extends HttpCaller<R> {

    Http1Caller(LinkedConfig config, Http1CallerBuilder<R> builder) {
        super(config, builder);
    }

    /**
     * Creates and returns a new {@link Http1CallerBuilder} instance for constructing
     * {@link Http1Caller} objects using a fluent API.
     *
     * @param returnType
     * @param config
     * @param <R>
     * @return
     */
    public static <R> Http1CallerBuilder<R> builder(TypeToken<R> returnType, LinkedConfig config) {
        return new Http1CallerBuilder<>(returnType, config);
    }
}
