package io.effi.rpc.transport;

import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.Invoker;
import io.effi.rpc.contract.context.InvocationContext;

/**
 * Represents a repackaged request.
 *
 * @param <T> the type of the invoker
 */
public interface RepackagedRequest<T extends Invoker<?>> extends RepackagedEnvelope<T, InvocationContext<Envelope.Request, T>> {

    /**
     * Encodes the request.
     */
    @Override
    RepackagedRequest<T> encode();

    @Override
    Envelope.Request envelope();

    default Envelope.Request request() {
        return envelope();
    }
}

