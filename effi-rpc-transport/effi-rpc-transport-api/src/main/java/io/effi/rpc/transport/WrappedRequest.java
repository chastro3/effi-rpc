package io.effi.rpc.transport;

import io.effi.rpc.base.Envelope;
import io.effi.rpc.base.Invoker;
import io.effi.rpc.base.context.InvocationContext;

/**
 * Represents a wrapped request.
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

