package io.effi.rpc.transport.idle;

import io.effi.rpc.component.event.Event;
import io.effi.rpc.component.event.EventLane;
import io.effi.rpc.transport.endpoint.Channel;

/**
 * Represents an event triggered when a channel becomes idle.
 */
public record IdleEvent(Channel channel) implements Event {

    @Override
    public EventLane lane() {
        return EventLane.CONTROL;
    }
}
