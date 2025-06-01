package io.effi.rpc.transport.netty;

import io.netty.channel.ChannelHandler;

/**
 * Wraps a {@link ChannelHandler} with its associated name.
 */
public record NamedChannelHandler(String name, ChannelHandler handler) {}

