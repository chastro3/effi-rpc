package io.effi.rpc.transport.endpoint;

import io.effi.rpc.async.Future;
import io.effi.rpc.async.Promise;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.transport.EndpointConfig;
import io.effi.rpc.context.Peer;
import io.effi.rpc.transport.TransportProtocol;
import io.effi.rpc.transport.TransportSupport;
import io.effi.rpc.transport.message.EncodableOutputMessage;
import io.effi.rpc.util.AbstractAttributes;
import io.effi.rpc.util.AssertUtil;

/**
 * Provides an abstract implementation of {@link Channel}.
 */
public abstract class AbstractChannel extends AbstractAttributes implements Channel {

    protected final Endpoint endpoint;

    protected final EndpointConfig config;

    protected final TransportProtocol protocol;

    /**
     * Resolves the protocol from the config, since an endpoint (especially a server) may support multiple protocols.
     *
     * @param endpoint the associated endpoint
     * @param config   the configuration used to determine the protocol
     */
    protected AbstractChannel(Endpoint endpoint, EndpointConfig config) {
        this.endpoint = AssertUtil.notNull(endpoint, "endpoint");
        this.config = AssertUtil.notNull(config, "config");
        this.protocol = endpoint.platform().namedExtension(TransportProtocol.class, config.protocolName());
    }

    @Override
    public Future<Void> send(Object message) {
        if (message instanceof EncodableOutputMessage<?> encodableOutputMessage) {
            Peer peer = encodableOutputMessage.context().peer();
            message = TransportSupport.inIOSerialization(peer)
                    ? encodableOutputMessage
                    : encodableOutputMessage.encode();
        }
        if (isActive()) {
            return doSend(message);
        }
        return Promise.completedVoid();
    }

    @Override
    public Endpoint endpoint() {
        return endpoint;
    }

    @Override
    public TransportProtocol protocol() {
        return protocol;
    }

    @Override
    public ScopedPlatform platform() {
        return endpoint.platform();
    }

    protected abstract Future<Void> doSend(Object message);
}
