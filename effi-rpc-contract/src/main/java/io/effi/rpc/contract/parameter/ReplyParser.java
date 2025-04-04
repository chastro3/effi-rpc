package io.effi.rpc.contract.parameter;

import io.effi.rpc.common.spi.Extensible;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.Result;

import static io.effi.rpc.common.constant.Component.DEFAULT;

/**
 * Handle the extraction and processing of response values.
 *
 * @param <RESP> the type of the response envelope that this resolver handles,
 *               which must extend the {@link Envelope} interface
 */
@Extensible(DEFAULT)
public interface ReplyParser<RESP extends Envelope.Response> {

    /**
     * Resolves the specified response into an appropriate object.
     * <p>
     * It may involve deserialization operations
     * and modifications to the response body. The resolved data
     * is returned in a format suitable for further processing or
     * for returning to the caller.
     * </p>
     *
     * @param response the response envelope containing the data to be resolved
     * @param caller   the caller object representing the entity that
     *                 initiated the request and expects a response
     */
    Result resolve(RESP response, Caller<?> caller);

}

