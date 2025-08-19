package io.effi.rpc.transport.idle;

import io.effi.rpc.component.event.AbstractEvent;
import io.effi.rpc.transport.endpoint.Channel;

/**
 * Represents an event triggered to refresh the idle count for a channel.
 */
public class RefreshIdleCountEvent extends AbstractEvent<Channel> {

    public RefreshIdleCountEvent(Channel channel) {
        super(channel);
    }
}
