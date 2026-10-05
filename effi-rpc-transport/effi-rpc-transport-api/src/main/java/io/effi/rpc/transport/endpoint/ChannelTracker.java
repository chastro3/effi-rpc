package io.effi.rpc.transport.endpoint;

import java.util.Collection;

/**
 * Tracks active channels for an endpoint.
 */
public interface ChannelTracker {

    /**
     * Adds a new channel to the active connection list.
     *
     * @param channel the channel to add
     */
    void add(Channel channel);

    /**
     * Removes a channel from the active connection list.
     *
     * @param channel the channel to remove
     */
    void remove(Channel channel);

    /**
     * Returns a snapshot of all currently active channels.
     */
    Collection<Channel> list();

    /**
     * Returns the number of active channels.
     */
    int size();

    /**
     * Supplies access to the {@link ChannelTracker}.
     */
    interface Supplier {

        /**
         * Returns the associated {@link ChannelTracker}.
         */
        ChannelTracker channelTracker();

    }
}
