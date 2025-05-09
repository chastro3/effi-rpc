package io.effi.rpc.transport;

import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.Invoker;
import io.effi.rpc.contract.context.InvocationContext;

/**
 * Represents a repackaged request.
 *
 * @param <T> the type of the invoker
 */
public interface WrappedRequest<T extends Invoker<?>> extends WrappedEnvelope<T, InvocationContext<Envelope.Request, T>> {

    /**
     * Encodes the request.
     */
    @Override
    WrappedRequest<T> encode();

    @Override
    Envelope.Request envelope();

    default Envelope.Request request() {
        return envelope();
    }
}

