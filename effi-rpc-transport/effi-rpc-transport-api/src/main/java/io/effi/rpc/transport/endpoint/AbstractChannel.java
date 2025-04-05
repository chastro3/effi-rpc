package io.effi.rpc.transport.endpoint;

import io.effi.rpc.common.config.URL;
import io.effi.rpc.common.util.AbstractAttributes;
import io.effi.rpc.contract.Invoker;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.transport.Protocol;
import io.effi.rpc.transport.RepackagedEnvelope;
import io.effi.rpc.transport.TransportSupport;

/**
 * Abstract implementation of {@link Channel}.
 */
public abstract class AbstractChannel extends AbstractAttributes implements Channel {

    private final URL endpointUrl;

    private final EffiRpcModule module;

    private final Protocol protocol;

    protected AbstractChannel(URL endpointUrl, EffiRpcModule module) {
        this.endpointUrl = endpointUrl;
        this.module = module;
        this.protocol = TransportSupport.getProtocol(endpointUrl.protocol());
    }

    @Override
    public void send(Object message) {
        if (message instanceof RepackagedEnvelope<?, ?> repackagedEnvelope) {
            Invoker<?> invoker = repackagedEnvelope.context().invoker();
            repackagedEnvelope = TransportSupport.inIOSerialization(invoker)
                    ? repackagedEnvelope
                    : repackagedEnvelope.encode();
            if (isActive()) doSend(repackagedEnvelope);
        }

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
