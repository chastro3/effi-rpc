package io.effi.rpc.transport.endpoint;

import io.effi.rpc.config.transport.ClientConfig;

import java.util.concurrent.CompletableFuture;

/**
 * Connects to a remote endpoint and manages channel(s).
 */
public interface Client extends Endpoint {

    @Override
    ClientConfig config();

    /**
     * Asynchronously gets a {@link Channel} associated with this client.
     */
    CompletableFuture<Channel> getChannel();
}


