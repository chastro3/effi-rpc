package io.effi.rpc.transport.idle;

import io.effi.rpc.component.event.AbstractEvent;
import io.effi.rpc.transport.endpoint.Channel;

/**
 * Represents an event triggered when a channel becomes idle.
 */
public class IdleEvent extends AbstractEvent<Channel> {

    public IdleEvent(Channel channel) {
        super(channel);
    }
}
