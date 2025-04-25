package io.effi.rpc.transport.endpoint;

import io.effi.rpc.config.URLSource;
import io.effi.rpc.util.Attributes;
import io.effi.rpc.util.resoruce.Closeable;
import io.effi.rpc.contract.module.ModuleSource;
import io.effi.rpc.transport.Protocol;

import java.net.InetSocketAddress;

/**
 * Communication channel for sending messages.
 */
public interface Channel extends Attributes, ModuleSource, URLSource, Closeable {

    /**
     * Sends a message through the channel.
     *
     * @param message the message to send
     */
    void send(Object message);

    /**
     * Returns the remote address of this channel.
     */
    InetSocketAddress remoteAddress();

    /**
     * Returns the local address of this channel.
     */
    InetSocketAddress localAddress();

    /**
     * Returns the protocol of this channel.
     */
    Protocol protocol();
}

