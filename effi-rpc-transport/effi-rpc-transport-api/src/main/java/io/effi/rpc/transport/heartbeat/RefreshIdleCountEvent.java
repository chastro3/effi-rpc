package io.effi.rpc.transport.heartbeat;

import io.effi.rpc.base.event.AbstractEvent;
import io.effi.rpc.transport.endpoint.Channel;

/**
 * Represents an event triggered to refresh the idle count for a channel.
 */
public class RefreshIdleCountEvent extends AbstractEvent<Channel> {

    public RefreshIdleCountEvent(Channel channel) {
        super(channel);
    }
}
