package io.effi.rpc.transport.endpoint;

/**
 * Client for connecting to a remote endpoint and get channel.
 */
public interface Client extends Endpoint {

    /**
     * Connects to the remote endpoint.
     */
    void connect();

    /**
     * Gets a {@link Channel} associated with this client.
     */
    Channel getChannel();

}

