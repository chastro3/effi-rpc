package io.effi.rpc.transport.endpoint;

import io.effi.rpc.async.Future;
import io.effi.rpc.component.transport.ServerConfig;

import java.net.InetSocketAddress;
import java.util.Collection;

/**
 * Accepts incoming connections and manages active channels.
 * <p>
 * Provides server functionality for binding to addresses, managing
 * client connections, and handling active communication channels.
 */
public interface Server extends Endpoint {

    /**
     * Binds to the configured address asynchronously.
     */
    Future<? extends Channel> bind();

    /**
     * Returns the local address bound to the server.
     */
    InetSocketAddress localAddress();

    /**
     * Returns active channels.
     */
    Collection<Channel> channels();

    /**
     * Looks up channel mapped to given remote address.
     *
     * @param remoteAddress the remote client address
     * @return the associated {@link Channel}, or {@code null} if not found
     */
    Channel lookupChannel(InetSocketAddress remoteAddress);

    /**
     * Returns the configuration of this server.
     */
    @Override
    ServerConfig config();
}



