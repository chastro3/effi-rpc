package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.config.NodeConfig;
import io.effi.rpc.util.TypeToken;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.protocol.http.HttpCaller;

/**
 * Http2 protocol implementation of {@link Caller}.
 *
 * @param <R>
 */
public class Http2Caller<R> extends HttpCaller<R> {

    Http2Caller(NodeConfig config, Http2CallerBuilder<R> builder) {
        super(config, builder);
    }

    /**
     * Creates and returns a new {@link Http2CallerBuilder} instance for constructing
     * {@link Http2Caller} objects using a fluent API.
     *
     * @param returnType
     * @param config
     * @param <R>
     * @return
     */
    public static <R> Http2CallerBuilder<R> builder(TypeToken<R> returnType, NodeConfig config) {
        return new Http2CallerBuilder<>(returnType, config);
    }
}
