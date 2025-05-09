package io.effi.rpc.contract.parameter;

import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.Result;
import io.effi.rpc.spi.Extensible;

import static io.effi.rpc.constant.Component.DEFAULT;

/**
 * Parses and converts response into result object.
 */
@Extensible(DEFAULT)
public interface ReplyParser<RESP extends Envelope.Response> {

    /**
     * Parses the given response and return a resolved result.
     * <p>
     * May involve deserialization and content transformation. The result is
     * suitable for downstream processing or returning to the original caller.
     * </p>
     *
     * @param response the response envelope to parse
     * @param caller   the caller that initiated the request
     * @return the resolved result
     */
    Result resolve(RESP response, Caller<?> caller);
}


