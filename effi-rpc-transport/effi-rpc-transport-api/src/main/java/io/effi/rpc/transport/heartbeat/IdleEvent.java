package io.effi.rpc.transport.heartbeat;

import io.effi.rpc.common.event.AbstractEvent;
import io.effi.rpc.transport.endpoint.Channel;

/**
 * HeartBeatEvent.
 */
public class IdleEvent extends AbstractEvent<Channel> {

    public IdleEvent(Channel channel) {
        super(channel);
    }
}
