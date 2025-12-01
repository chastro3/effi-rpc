package io.effi.rpc.transport.netty;

import io.netty.channel.ChannelHandler;

/**
 * Wraps a {@link ChannelHandler} with its associated id.
 */
public record NamedChannelHandler(String name, ChannelHandler handler) {

    public NamedChannelHandler(ChannelHandler handler) {
        this(handler.getClass().getSimpleName(), handler);
    }

}

