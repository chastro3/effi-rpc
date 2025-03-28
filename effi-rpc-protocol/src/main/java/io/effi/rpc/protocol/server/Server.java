package io.effi.rpc.protocol.server;

import io.effi.rpc.protocol.endpoint.Endpoint;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.Collection;

/**
 * Server that handles incoming connections and facilitates communication.
 */
public interface Server extends Endpoint {

    /**
     * Binds the server to the specified host and port,
     * making it ready to accept incoming connections.
     */
    void bind();

    /**
     * Returns all channels associated with the server.
     *
     * @return An array of {@link Channel} objects representing
     * all channels associated with the server
     */
    Collection<Channel> channels();

    /**
     * Returns the channel associated with the specified remote address.
     *
     * @param remoteAddress The remote address of the client whose associated
     *                      channel is to be retrieved.
     * @return The {@link Channel} associated with the specified remote address,
     * or null if no channel is found for the given address.
     */
    Channel acquireChannel(InetSocketAddress remoteAddress);

}

