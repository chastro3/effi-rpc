package io.effi.rpc.transport.endpoint;

import io.effi.rpc.config.URL;
import io.effi.rpc.util.AbstractAttributes;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.contract.Invoker;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.transport.Protocol;
import io.effi.rpc.transport.WrappedEnvelope;
import io.effi.rpc.transport.TransportSupport;

/**
 * Provides an abstract implementation of {@link Channel}.
 */
public abstract class AbstractChannel extends AbstractAttributes implements Channel {

    protected final URL endpointUrl;

    protected final EffiRpcModule module;

    protected final Protocol protocol;

    protected AbstractChannel(URL endpointUrl, EffiRpcModule module) {
        this.endpointUrl = AssertUtil.notNull(endpointUrl, "endpointUrl");
        this.module = AssertUtil.notNull(module, "module");
        this.protocol = TransportSupport.getProtocol(endpointUrl.protocol());
    }

    @Override
    public void send(Object message) {
        if (message instanceof WrappedEnvelope<?, ?> wrappedEnvelope) {
            Invoker<?> invoker = wrappedEnvelope.context().invoker();
            message = TransportSupport.inIOSerialization(invoker)
                    ? wrappedEnvelope
                    : wrappedEnvelope.encode().envelope();
        }
        if (isActive()) doSend(message);
    }

    @Override
    public Protocol protocol() {
        return protocol;
    }

    @Override
    public URL url() {
        return endpointUrl;
    }

    @Override
    public EffiRpcModule module() {
        return module;
    }

    @Override
    public String toString() {
        return String.format("local=%s, remote=%s, active=%b", localAddress(), remoteAddress(), isActive());
    }

    protected abstract void doSend(Object message);
}
