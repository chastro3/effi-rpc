package io.effi.rpc.transport.endpoint;

import io.effi.rpc.config.URL;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.util.AssertUtil;

import java.net.InetSocketAddress;

/**
 * Provides an abstract implementation of {@link Endpoint}.
 */
public abstract class AbstractEndpoint implements Endpoint {

    protected URL url;

    protected EffiRpcModule module;

    protected InetSocketAddress address;

    protected AbstractEndpoint(URL url, EffiRpcModule module) {
        this.url = AssertUtil.notNull(url, "url");
        this.module = AssertUtil.notNull(module, "module");
        this.address = InetSocketAddress.createUnresolved(url.host(), url.port());
    }

    @Override
    public EffiRpcModule module() {
        return module;
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
