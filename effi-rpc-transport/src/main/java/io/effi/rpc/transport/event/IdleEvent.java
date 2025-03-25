package io.effi.rpc.transport.event;

import io.effi.rpc.common.event.AbstractEvent;
import io.effi.rpc.transport.NettyChannel;

/**
 * HeartBeatEvent.
 */
public class IdleEvent extends AbstractEvent<NettyChannel> {

    public IdleEvent(NettyChannel channel) {
        super(channel);
    }

}
