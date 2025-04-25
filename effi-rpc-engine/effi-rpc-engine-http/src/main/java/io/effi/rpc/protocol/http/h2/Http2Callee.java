package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.config.NodeConfig;
import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.parameter.MethodMapper;
import io.effi.rpc.protocol.http.HttpCallee;

/**
 * Http2 protocol implementation of {@link Callee}.
 *
 * @param <T>
 */
public class Http2Callee<T> extends HttpCallee<T> {

    Http2Callee(NodeConfig config, Http2CalleeBuilder<T> builder) {
        super(config, builder);
    }

    /**
     * Creates and returns a new {@link Http2CalleeBuilder} instance for constructing
     * {@link Http2Callee} objects using a fluent API.
     *
     * @param methodMapper
     * @param config
     * @param <T>
     * @return a new {@link Http2CalleeBuilder} instance
     */
    public static <T> Http2CalleeBuilder<T> builder(MethodMapper<T> methodMapper, NodeConfig config) {
        return new Http2CalleeBuilder<>(methodMapper, config);
    }

}
