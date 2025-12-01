package io.effi.rpc.transport.endpoint;

import io.effi.rpc.concurrent.Future;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.transport.TransportProtocol;
import io.effi.rpc.util.Attributes;
import io.effi.rpc.trait.Closeable;

import java.net.InetSocketAddress;

/**
 * Handles message transmission over communication channels.
 * <p>
 * Provides channel functionality for sending messages between endpoints
 * with address management and protocol support.
 */
public interface Channel extends Attributes, ScopedPlatform.Supplier, TransportProtocol.Supplier, Closeable {

    /**
     * Sends a message through this channel.
     *
     * @param message the message to send
     */
    Future<Void> send(Object message);

    /**
     * Returns the associated endpoint.
     */
    Endpoint endpoint();

    /**
     * Returns the remote address.
     */
    InetSocketAddress remoteAddress();

    /**
     * Returns the local address.
     */
    InetSocketAddress localAddress();
}


