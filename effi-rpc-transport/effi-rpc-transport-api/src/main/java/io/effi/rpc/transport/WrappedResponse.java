package io.effi.rpc.transport;

import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.Invoker;
import io.effi.rpc.contract.context.ReplyContext;

/**
 * Represents a repackaged response.
 *
 * @param <T> the type of the invoker
 */
public interface WrappedResponse<T extends Invoker<?>> extends WrappedEnvelope<T, ReplyContext<Envelope.Response, T>> {

    /**
     * Encodes the response.
     */
    @Override
    WrappedResponse<T> encode();

    @Override
    Envelope.Response envelope();

    default Envelope.Response response() {
        return envelope();
    }
}
