package io.effi.rpc.transport.endpoint;



import java.net.InetSocketAddress;
import java.util.Collection;

/**
 * Server for handling incoming connections and communication.
 */
public interface Server extends Endpoint {

    /**
     * Binds the server, making it ready to accept connections.
     */
    void bind();

    /**
     * Returns all active channels.
     *
     * @return a collection of active {@link Channel} instances
     */
    Collection<Channel> channels();

    /**
     * Retrieves the channel associated with the given remote address.
     *
     * @param remoteAddress the remote client address
     * @return the associated {@link Channel}, or {@code null} if not found
     */
    Channel acquireChannel(InetSocketAddress remoteAddress);
}


