package io.effi.rpc.protocol;

import io.effi.rpc.common.url.URL;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.netty.handler.ssl.SslContext;

import java.util.List;
import java.util.function.Supplier;

/**
 * Endpoint's initialize configuration.
 *
 * @param url
 * @param module
 * @param initializedHandlers
 * @param sslContext
 */
public record InitializedConfig(URL url, EffiRpcModule module,
                                Supplier<List<NamedChannelHandler>> initializedHandlers,
                                SslContext sslContext) {

}
