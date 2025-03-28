package io.effi.rpc.protocol.event;

import io.effi.rpc.common.event.AbstractEvent;
import io.effi.rpc.protocol.NettyChannel;

/**
 * HeartBeatEvent.
 */
public class IdleEvent extends AbstractEvent<NettyChannel> {

    public IdleEvent(NettyChannel channel) {
        super(channel);
    }

}
