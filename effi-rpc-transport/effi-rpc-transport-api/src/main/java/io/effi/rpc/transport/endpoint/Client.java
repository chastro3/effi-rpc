package io.effi.rpc.transport.endpoint;

import io.effi.rpc.component.transport.ClientConfig;
import io.effi.rpc.concurrent.Future;

import java.net.InetSocketAddress;

/**
 * Connects to remote servers and manages client channels.
 */
public interface Client extends Endpoint {

    /**
     * Returns the remote address connected to the client.
     */
    InetSocketAddress remoteAddress();

    /**
     * Fetches the {@link Channel} associated with this client asynchronously.
     */
    Future<? extends Channel> fetchChannel();

    /**
     * Releases a channel previously obtained from {@link #fetchChannel()}.
     * <p>
     * The default keeps non-pooled channels open for reuse; pooled clients override
     * this method to return the channel to their pool.
     *
     * @param channel the channel to release
     */
    default void release(Channel channel) {
    }

    /**
     * Discards a channel that can no longer be reused.
     * <p>
     * Pooled clients must release the underlying pool slot even when the channel is closed.
     *
     * @param channel the channel to discard
     */
    default void discard(Channel channel) {
        channel.close();
    }

    /**
     * Returns the configuration of this client.
     */
    @Override
    ClientConfig config();
}


