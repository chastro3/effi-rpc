package io.effi.rpc.transport;

import io.effi.rpc.base.Envelope;
import io.effi.rpc.base.Invoker;
import io.effi.rpc.base.context.ReplyContext;

/**
 * Represents a wrapped response.
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
