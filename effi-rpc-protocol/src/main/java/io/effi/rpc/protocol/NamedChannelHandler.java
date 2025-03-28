package io.effi.rpc.protocol;

import io.netty.channel.ChannelHandler;

/**
 * Combines a name with a corresponding {@link ChannelHandler}.
 *
 * @param name    the name of the channel handler
 * @param handler the channel handler instance
 */
public record NamedChannelHandler(String name, ChannelHandler handler) {}

