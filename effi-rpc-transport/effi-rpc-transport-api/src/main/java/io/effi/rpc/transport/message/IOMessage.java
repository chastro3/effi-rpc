package io.effi.rpc.transport.message;

import io.effi.rpc.context.Message;
import io.effi.rpc.transport.TransportProtocol;
import io.effi.rpc.transport.endpoint.Channel;

/**
 * Represents transport-layer messages for I/O operations.
 * <p>
 * Provides message functionality for transport layer communication with
 * channel association and protocol support.
 *
 * @see InputMessage
 * @see OutputMessage
 */
public interface IOMessage extends Message, TransportProtocol.Supplier {

    /**
     * Returns the associated channel.
     */
    Channel channel();

    @Override
    default TransportProtocol protocol() {
        return channel().protocol();
    }
}


