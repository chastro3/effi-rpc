package io.effi.rpc.transport.event;

import io.effi.rpc.common.event.AbstractEvent;
import io.effi.rpc.transport.NettyChannel;

/**
 * HeartBeatEvent.
 */
public class RefreshHeartBeatCountEvent extends AbstractEvent<NettyChannel> {

    private final Integer readIdeTimes;

    private final Integer writeIdeTimes;

    private final Integer allIdeTimes;

    public RefreshHeartBeatCountEvent(NettyChannel channel, Integer readIdeTimes, Integer writeIdeTimes, Integer allIdeTimes) {
        super(channel);
        this.readIdeTimes = readIdeTimes;
        this.writeIdeTimes = writeIdeTimes;
        this.allIdeTimes = allIdeTimes;
    }

    /**
     * Create for client.
     *
     * @param channel
     * @return
     */
    public static RefreshHeartBeatCountEvent buildForClient(NettyChannel channel) {
        return new RefreshHeartBeatCountEvent(channel, null, 0, 0);
    }

    /**
     * Create for server.
     *
     * @param channel
     * @return
     */
    public static RefreshHeartBeatCountEvent buildForServer(NettyChannel channel) {
        return new RefreshHeartBeatCountEvent(channel, 0, null, 0);
    }

    /**
     * Returns the readIdeTimes.
     *
     * @return the readIdeTimes
     */
    public Integer readIdeTimes() {
        return readIdeTimes;
    }

    /**
     * Returns the writeIdeTimes.
     *
     * @return the writeIdeTimes
     */
    public Integer writeIdeTimes() {
        return writeIdeTimes;
    }

    /**
     * Returns the allIdeTimes.
     *
     * @return the allIdeTimes
     */
    public Integer allIdeTimes() {
        return allIdeTimes;
    }

}
