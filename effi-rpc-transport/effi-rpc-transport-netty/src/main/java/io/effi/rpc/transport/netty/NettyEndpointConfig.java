package io.effi.rpc.transport.netty;

import io.effi.rpc.config.URL;
import io.effi.rpc.config.URLSource;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.contract.module.ModuleSource;
import io.effi.rpc.util.AssertUtil;
import io.netty.handler.ssl.SslContext;

import java.util.List;
import java.util.function.Function;

/**
 * Endpoint's initialize configuration.
 */
public class NettyEndpointConfig implements URLSource, ModuleSource {
    private final URL url;

    private final EffiRpcModule module;

    private SslContext sslContext;

    private Function<NettyEndpointConfig, NamedChannelHandler> codecInitializer;

    private Function<NettyEndpointConfig, List<NamedChannelHandler>> handlersInitializer;

    public NettyEndpointConfig(URL url, EffiRpcModule module) {
        this.url = AssertUtil.notNull(url, "url");
        this.module = AssertUtil.notNull(module, "module");
    }

    public NettyEndpointConfig sslContext(SslContext sslContext) {
        this.sslContext = sslContext;
        return this;
    }

    public NettyEndpointConfig codecInitializer(Function<NettyEndpointConfig, NamedChannelHandler> creator) {
        this.codecInitializer = creator;
        return this;
    }

    public NettyEndpointConfig handlersInitializer(Function<NettyEndpointConfig, List<NamedChannelHandler>> creator) {
        this.handlersInitializer = creator;
        return this;
    }

    @Override
    public URL url() {
        return url;
    }

    @Override
    public EffiRpcModule module() {
        return module;
    }

    public SslContext sslContext() {
        return sslContext;
    }

    public NamedChannelHandler codec() {
        return codecInitializer.apply(this);
    }

    public List<NamedChannelHandler> handlers() {
        return handlersInitializer.apply(this);
    }
}
