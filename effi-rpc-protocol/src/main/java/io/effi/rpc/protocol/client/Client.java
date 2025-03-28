package io.effi.rpc.protocol.client;

import io.effi.rpc.protocol.NettyChannel;
import io.effi.rpc.protocol.endpoint.Endpoint;
import io.netty.channel.Channel;

/**
 * Client that can connect to a remote endpoint and send messages.
 */
public interface Client extends Endpoint {

    /**
     * Connects to the remote endpoint.
     */
    void connect();

    /**
     * Returns the {@link Channel} associated with this client.
     */
    NettyChannel acquireChannel();

}

