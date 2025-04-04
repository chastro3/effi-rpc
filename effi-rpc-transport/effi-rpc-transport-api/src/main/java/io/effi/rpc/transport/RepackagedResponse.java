package io.effi.rpc.transport;

import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.Invoker;
import io.effi.rpc.contract.context.ReplyContext;

/**
 * Represents a repackaged response.
 *
 * @param <T> the type of the invoker
 */
public interface RepackagedResponse<T extends Invoker<?>> extends RepackagedEnvelope<T, ReplyContext<Envelope.Response, T>> {

    /**
     * Encodes the response.
     */
    @Override
    RepackagedResponse<T> encode();

    /**
     * Returns the current response, which may have been encoded.
     */
    Envelope.Response response();
}
