package io.effi.rpc.transport;

import io.effi.rpc.base.CallSide;
import io.effi.rpc.base.Message;
import io.effi.rpc.base.context.ExchangeContext;
import io.effi.rpc.transport.endpoint.Channel;

/**
 * Represents a wrapped envelope.
 */
public interface WrappedEnvelope<I extends CallSide, C extends ExchangeContext<?, ?>> {

    /**
     * Returns the context.
     */
    C context();

    /**
     * Returns the channel.
     */
    Channel channel();

    /**
     * Returns the envelope.
     */
    Message envelope();

    /**
     * Encodes the envelope.
     */
    WrappedEnvelope<I, C> encode();
}


