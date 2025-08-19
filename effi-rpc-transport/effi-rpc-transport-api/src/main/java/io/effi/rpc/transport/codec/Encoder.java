package io.effi.rpc.transport.codec;

import io.effi.rpc.transport.endpoint.Channel;
import io.effi.rpc.transport.message.OutputMessage;

/**
 * Encodes input objects into output messages using channels.
 * <p>
 * Provides encoder functionality for converting input objects into
 * output messages suitable for transport layer communication.
 */
@FunctionalInterface
public interface Encoder<IN> {

    /**
     * Encodes the specified input using the provided channel.
     *
     * @param input   the input to encode
     * @param channel the associated channel
     * @return the encoded output message
     */
    OutputMessage encode(IN input, Channel channel);
}

