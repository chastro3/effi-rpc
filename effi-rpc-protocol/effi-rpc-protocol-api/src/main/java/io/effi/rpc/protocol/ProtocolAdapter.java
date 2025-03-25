package io.effi.rpc.protocol;

import io.effi.rpc.common.extension.TypeToken;
import io.effi.rpc.common.url.Config;
import io.effi.rpc.contract.*;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.contract.parameter.MethodMapper;

/**
 * Handle invocation-related operations in the RPC protocol.
 */
public interface ProtocolAdapter {

    /**
     * Creates a request envelope for the given caller and arguments.
     *
     * @param caller the caller initiating the request
     * @param args   the arguments for the request
     * @return the created request envelope
     */
    Envelope.Request createRequest(Caller<?> caller, Object[] args);

    /**
     * Creates a response envelope for the given callee and result.
     *
     * @param callee the callee handling the response
     * @param result the result to include in the response
     * @return the created response envelope
     */
    Envelope.Response createResponse(Callee<?> callee, Result result);

    <T> Callee<T> createCallee(MethodMapper<T> methodMapper, Config config);

    <T> Caller<T> createCaller(TypeToken<T> returnType, EffiRpcModule module, Locator locator, Config config);

    /**
     * Returns the supported type for request envelopes.
     *
     * @return the supported request envelope type
     */
    Class<? extends Envelope.Request> supportedRequestType();

    /**
     * Returns the supported type for response envelopes.
     *
     * @return the supported response envelope type
     */
    Class<? extends Envelope.Response> supportedResponseType();

}

