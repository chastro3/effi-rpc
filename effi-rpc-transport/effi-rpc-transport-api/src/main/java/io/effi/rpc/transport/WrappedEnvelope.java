package io.effi.rpc.transport;

import io.effi.rpc.base.Envelope;
import io.effi.rpc.base.Invoker;
import io.effi.rpc.base.context.ExecutorContext;
import io.effi.rpc.transport.endpoint.Channel;

/**
 * Represents a wrapped envelope.
 */
public interface WrappedEnvelope<I extends Invoker<?>, C extends ExecutorContext<?, ?, ?>> {

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
    Envelope envelope();

    /**
     * Encodes the envelope.
     */
    WrappedEnvelope<I, C> encode();
}


