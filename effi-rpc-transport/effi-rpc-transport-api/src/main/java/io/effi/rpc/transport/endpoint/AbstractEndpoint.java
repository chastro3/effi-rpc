package io.effi.rpc.transport.endpoint;

import io.effi.rpc.common.config.URL;
import io.effi.rpc.common.util.AssertUtil;
import io.effi.rpc.contract.module.EffiRpcModule;


import java.net.InetSocketAddress;

/**
 * Abstract implementation of {@link Endpoint}.
 */
public abstract class AbstractEndpoint implements Endpoint {

    protected URL url;

    protected EffiRpcModule module;

    protected InetSocketAddress address;

    protected AbstractEndpoint(URL url, EffiRpcModule module) {
        this.url = AssertUtil.notNull(url, "url");
        this.module = AssertUtil.notNull(module, "module");
        this.address = new InetSocketAddress(url.host(), url.port());
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
