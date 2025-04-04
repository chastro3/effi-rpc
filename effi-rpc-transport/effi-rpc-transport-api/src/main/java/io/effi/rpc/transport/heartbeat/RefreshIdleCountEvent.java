package io.effi.rpc.transport.heartbeat;

import io.effi.rpc.common.event.AbstractEvent;
import io.effi.rpc.transport.endpoint.Channel;

/**
 * Represents an event for refreshing the heartbeat count.
 */
public class RefreshIdleCountEvent extends AbstractEvent<Channel> {

    public RefreshIdleCountEvent(Channel channel) {
        super(channel);
    }
}
