package io.effi.rpc.transport.endpoint;

/**
 * Represents a client that connects to a remote endpoint and manages channel(s).
 */
public interface Client extends Endpoint {

    /**
     * Connects to the remote server.
     */
    void connect();

    /**
     * Gets a {@link Channel} associated with this client.
     */
    Channel getChannel();

}

