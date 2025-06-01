package io.effi.rpc.transport.endpoint;

import io.effi.rpc.component.EffiRpcPlatform;
import io.effi.rpc.config.DefaultConfigKeys;
import io.effi.rpc.config.transport.ClientConfig;
import io.effi.rpc.util.StringUtil;

import java.net.InetSocketAddress;

/**
 * Provides an abstract implementation of {@link Client}.
 */
public abstract class AbstractClient extends AbstractEndpoint implements Client {

    protected int connectTimeout;

    protected AbstractClient(ClientConfig config, InetSocketAddress address, EffiRpcPlatform platform) {
        super(config, address, platform);
        this.connectTimeout = url().getIntParam(DefaultConfigKeys.CONNECT_TIMEOUT);
        initialize();
    }

    @Override
    public ClientConfig config() {
        return (ClientConfig) config;
    }

    @Override
    public String toString() {
        return StringUtil.format("config={}, active={}", url(), isActive());
    }

    protected abstract void initialize();

}
