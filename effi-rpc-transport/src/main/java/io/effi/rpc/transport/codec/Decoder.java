package io.effi.rpc.transport.codec;

import io.effi.rpc.transport.NettyChannel;

/**
 * Decode messages.
 *
 * @param <I> the type of the input (encoded message)
 * @param <O> the type of the decoded output message
 */
public interface Decoder<I, O> {

    /**
     * Decodes the given encoded message.
     *
     * @param channel the channel used for communication
     * @param message the encoded message to decode
     * @return the decoded message
     */
    O decode(NettyChannel channel, I message);
}
