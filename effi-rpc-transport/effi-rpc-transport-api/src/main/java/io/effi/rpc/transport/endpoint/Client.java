package io.effi.rpc.transport.endpoint;

import io.effi.rpc.component.transport.ClientConfig;
import io.effi.rpc.concurrent.Future;

import java.net.InetSocketAddress;

/**
 * Connects to remote servers and manages communication channels.
 * <p>
 * Provides client functionality for establishing connections to remote
 * servers and managing associated communication channels.
 */
public interface Client extends Endpoint {

    /**
     * Returns the remote address connected to the client.
     */
    InetSocketAddress remoteAddress();

    /**
     * Fetches the {@link Channel} associated with this client asynchronously.
     */
    Future<? extends Channel> fetchChannel();

    /**
     * Returns the configuration of this client.
     */
    @Override
    ClientConfig config();
}


