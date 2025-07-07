package io.effi.rpc.transport.endpoint;

import io.effi.rpc.base.CallSide;
import io.effi.rpc.component.EffiRpcPlatform;
import io.effi.rpc.config.URL;
import io.effi.rpc.transport.Protocol;
import io.effi.rpc.transport.TransportSupport;
import io.effi.rpc.transport.WrappedEnvelope;
import io.effi.rpc.util.AbstractAttributes;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.StringUtil;

import java.util.concurrent.CompletableFuture;

/**
 * Provides an abstract implementation of {@link Channel}.
 */
public abstract class AbstractChannel extends AbstractAttributes implements Channel {

    protected final Endpoint endpoint;

    protected final URL url;

    protected final Protocol protocol;

    protected AbstractChannel(Endpoint endpoint, URL url) {
        this.endpoint = AssertUtil.notNull(endpoint, "endpoint");
        this.url = AssertUtil.notNull(url, "url");
        this.protocol = TransportSupport.getProtocol(url.protocol());
    }

    @Override
    public CompletableFuture<Channel> send(Object message) {
        if (message instanceof WrappedEnvelope<?, ?> wrappedEnvelope) {
            CallSide callSide = wrappedEnvelope.context().callSide();
            message = TransportSupport.inIOSerialization(callSide)
                    ? wrappedEnvelope
                    : wrappedEnvelope.encode().envelope();
        }
        if (isActive()) {
            return doSend(message);
        }
        return CompletableFuture.completedFuture(this);
    }

    @Override
    public Protocol protocol() {
        return protocol;
    }

    @Override
    public URL url() {
        return url;
    }

    @Override
    public EffiRpcPlatform platform() {
        return endpoint.platform();
    }

    @Override
    public String toString() {
        return StringUtil.format("local={}, remote={}, active={}", localAddress(), remoteAddress(), isActive());
    }

    protected abstract CompletableFuture<Channel> doSend(Object message);
}
