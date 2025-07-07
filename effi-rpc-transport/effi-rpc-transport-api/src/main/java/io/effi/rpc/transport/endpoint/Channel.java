package io.effi.rpc.transport.endpoint;

import io.effi.rpc.component.EffiRpcPlatform;
import io.effi.rpc.config.URL;
import io.effi.rpc.transport.Protocol;
import io.effi.rpc.util.Attributes;
import io.effi.rpc.util.resoruce.Closeable;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * Handles message transmission over a communication channel.
 */
public interface Channel extends Attributes, EffiRpcPlatform.Provider, URL.Provider, Closeable {

    /**
     * Sends a message through this channel.
     *
     * @param message the message to send
     */
    CompletableFuture<Channel> send(Object message);

    /**
     * Returns remote address.
     */
    InetSocketAddress remoteAddress();

    /**
     * Returns local address.
     */
    InetSocketAddress localAddress();

    /**
     * Returns protocol.
     */
    Protocol protocol();
}


