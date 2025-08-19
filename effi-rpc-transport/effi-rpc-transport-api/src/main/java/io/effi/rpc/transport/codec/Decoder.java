package io.effi.rpc.transport.codec;

import io.effi.rpc.transport.message.InputMessage;

/**
 * Decodes input messages into output objects using target contexts.
 * <p>
 * Provides decoder functionality for converting input messages into
 * output objects with optional target context support.
 */
@FunctionalInterface
public interface Decoder<OUT, T> {

    /**
     * Decodes the given input message into an output object.
     *
     * @param inputMessage the message to decode
     * @param target       the target context or object used in decoding
     * @return the decoded output object
     */
    OUT decode(InputMessage inputMessage, T target);
}

