package io.effi.rpc.transport;

import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.Invoker;
import io.effi.rpc.contract.context.ExecutorContext;
import io.effi.rpc.transport.endpoint.Channel;

/**
 * Represents a repackaged envelope.
 *
 * @param <I> the type of the invoker
 * @param <C> the type of the executor context
 */
public interface RepackagedEnvelope<I extends Invoker<?>, C extends ExecutorContext<?, ?, ?>> {

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
    RepackagedEnvelope<I, C> encode();
}


