package io.effi.rpc.transport.message;

import io.effi.rpc.context.Message;
import io.effi.rpc.transport.TransportProtocol;
import io.effi.rpc.transport.endpoint.Channel;

/**
 * Represents a transport-layer I/O message bound to a channel.
 *
 * @see InputMessage
 * @see OutputMessage
 */
public interface IOMessage extends Message, TransportProtocol.Supplier, AutoCloseable {

    /**
     * Returns the associated channel.
     */
    Channel channel();

    @Override
    default TransportProtocol protocol() {
        return channel().protocol();
    }

    /**
     * Releases resources associated with this message.
     */
    @Override
    void close();
}


