package io.effi.rpc.transport.endpoint;

import io.effi.rpc.component.EffiRpcPlatform;
import io.effi.rpc.config.URL;
import io.effi.rpc.config.transport.EndpointConfig;
import io.effi.rpc.util.AssertUtil;

import java.net.InetSocketAddress;

/**
 * Provides an abstract implementation of {@link Endpoint}.
 */
public abstract class AbstractEndpoint extends EffiRpcPlatform.Holder implements Endpoint {

    protected EndpointConfig config;

    protected InetSocketAddress address;

    protected URL url;

    protected AbstractEndpoint(EndpointConfig config, InetSocketAddress address, EffiRpcPlatform platform) {
        super(platform);
        this.config = AssertUtil.notNull(config, "config");
        this.address = AssertUtil.notNull(address, "address");
        this.url = config.newUrl(address);
    }

    @Override
    public URL url() {
        return url;
    }

    @Override
    public String host() {
        return url().host();
    }

    @Override
    public int port() {
        return url().port();
    }

    @Override
    public InetSocketAddress socketAddress() {
        return address;
    }

}
