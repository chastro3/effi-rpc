package io.effi.rpc.transport.client;

import io.effi.rpc.common.exception.EffiRpcException;
import io.effi.rpc.transport.NettyChannel;
import io.effi.rpc.transport.endpoint.Endpoint;
import io.netty.channel.Channel;

import java.net.ConnectException;

/**
 * Client that can connect to a remote endpoint and send messages.
 */
public interface Client extends Endpoint {

    /**
     * Connects to the remote endpoint.
     *
     * @throws ConnectException if an error occurs during the connecting process
     */
    void connect() throws ConnectException;

    /**
     * Returns the {@link Channel} associated with this client.
     *
     * @return the {@link Channel} used by this client
     * @throws EffiRpcException if an error occurs during the acquiring process
     */
    NettyChannel acquireChannel() throws EffiRpcException;

}

