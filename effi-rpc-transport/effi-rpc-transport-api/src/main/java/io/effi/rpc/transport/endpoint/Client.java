package io.effi.rpc.transport.endpoint;



/**
 * Client that can connect to a remote endpoint and acquire channel(s).
 */
public interface Client extends Endpoint {

    /**
     * Connects to the remote endpoint.
     */
    void connect();

    /**
     * Acquires a {@link Channel} associated with this client.
     */
    Channel acquireChannel();

}

