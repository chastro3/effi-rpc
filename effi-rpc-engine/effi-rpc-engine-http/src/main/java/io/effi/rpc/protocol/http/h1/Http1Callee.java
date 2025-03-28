package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.common.url.Config;
import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.parameter.MethodMapper;
import io.effi.rpc.protocol.http.HttpCallee;

/**
 * Http1.1 protocol implementation of {@link Callee}.
 *
 * @param <T>
 */
public class Http1Callee<T> extends HttpCallee<T> {

    Http1Callee(Config config, Http1CalleeBuilder<T> builder) {
        super(config, builder);
    }

    /**
     * Creates and returns a new {@link Http1CalleeBuilder} instance for constructing
     * {@link Http1Callee} objects using a fluent API.
     *
     * @param methodMapper
     * @param config
     * @param <T>
     * @return a new {@link Http1CalleeBuilder} instance
     */
    public static <T> Http1CalleeBuilder<T> builder(MethodMapper<T> methodMapper, Config config) {
        return new Http1CalleeBuilder<>(methodMapper, config);
    }

}
