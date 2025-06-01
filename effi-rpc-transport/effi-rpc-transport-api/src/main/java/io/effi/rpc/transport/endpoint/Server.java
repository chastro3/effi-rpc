package io.effi.rpc.transport.endpoint;

import io.effi.rpc.config.transport.ServerConfig;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * Accepts incoming connections and manages active channels.
 */
public interface Server extends Endpoint {

    /**
     * Asynchronously binds to the configured address.
     */
    CompletableFuture<Void> bind();

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

    @Override
    ServerConfig config();
}



