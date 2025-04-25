package io.effi.rpc.transport.heartbeat;

import io.effi.rpc.event.AbstractEvent;
import io.effi.rpc.transport.endpoint.Channel;

/**
 * Represents an event triggered when a channel becomes idle.
 */
public class IdleEvent extends AbstractEvent<Channel> {

    public IdleEvent(Channel channel) {
        super(channel);
    }
}
