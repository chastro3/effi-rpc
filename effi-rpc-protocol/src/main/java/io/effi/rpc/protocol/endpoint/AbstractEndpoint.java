package io.effi.rpc.protocol.endpoint;

import io.effi.rpc.common.url.URL;
import io.effi.rpc.common.util.AssertUtil;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.protocol.InitializedConfig;

import java.net.InetSocketAddress;

/**
 * Abstract implementation of {@link Endpoint}.
 */
public abstract class AbstractEndpoint implements Endpoint {

    protected InetSocketAddress address;

    protected InitializedConfig config;

    protected AbstractEndpoint(InitializedConfig config) {
        this.config = AssertUtil.notNull(config, "config");
        URL url = config.url();
        this.address = new InetSocketAddress(url.host(), url.port());
    }

    @Override
    public EffiRpcModule module() {
        return config.module();
    }

    @Override
    public URL url() {
        return config.url();
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
