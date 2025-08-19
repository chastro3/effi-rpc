package io.effi.rpc.transport.endpoint;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.transport.EndpointConfig;
import io.effi.rpc.transport.TransportProtocol;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.NetUtil;

import java.net.InetSocketAddress;

/**
 * Provides an abstract implementation of {@link Endpoint}.
 */
public abstract class AbstractEndpoint extends ScopedPlatform.Holder implements Endpoint {

    protected EndpointConfig config;

    protected TransportProtocol protocol;

    protected InetSocketAddress address;

    protected AbstractEndpoint(EndpointConfig config, InetSocketAddress address, ScopedPlatform platform) {
        super(platform);
        this.config = AssertUtil.notNull(config, "config");
        this.address = NetUtil.resolveIfUnresolved(AssertUtil.notNull(address, "address"));
        this.protocol = platform.namedExtension(TransportProtocol.class, config.protocolName());
    }

    @Override
    public EndpointConfig config() {
        return config;
    }

    @Override
    public TransportProtocol protocol() {
        return protocol;
    }
}
