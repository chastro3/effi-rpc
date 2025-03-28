package io.effi.rpc.protocol.codec;

import io.effi.rpc.protocol.NettyChannel;

/**
 * Encode messages.
 *
 * @param <I> the type of the input message
 * @param <O> the type of the encoded output
 */
public interface Encoder<I, O> {

    /**
     * Encodes the given message.
     *
     * @param channel the channel used for communication
     * @param message the message to encode
     * @return the encoded message
     */
    O encode(NettyChannel channel, I message);
}

