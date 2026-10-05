package io.effi.rpc.transport.endpoint;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.concurrent.Future;
import io.effi.rpc.trait.Closeable;
import io.effi.rpc.transport.TransportProtocol;
import io.effi.rpc.util.Attributes;

import java.net.InetSocketAddress;

/**
 * Handles message transmission over communication channels.
 */
public interface Channel extends Attributes, ScopedPlatform.Supplier, TransportProtocol.Supplier, Closeable {

    /**
     * Sends a message through this channel.
     *
     * @param message the message to send
     * @return a future completed when the message is written
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


