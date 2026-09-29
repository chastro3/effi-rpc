package io.effi.rpc.context;

import io.effi.rpc.context.invocation.Invocation;

/**
 * Creates protocol-specific request and response messages.
 * <p>
 * Provides a factory interface for creating RPC messages including
 * requests from caller(s) and responses from servant(s).
 */
public interface MessageFactory {

    /**
     * Creates a request from the specified caller and arguments.
     *
     * @param caller the caller initiating the request
     * @param invocation the method invocation
     * @return the created request
     */
    Request createRequest(Caller<?> caller, Invocation invocation);

    /**
     * Creates a response from the specified servant and result.
     *
     * @param servant the servant handling the request
     * @param result the result of the invocation
     * @return the created response
     */
    Response createResponse(Servant servant, Interaction.Result result);

    /**
     * Returns the request type.
     */
    Class<? extends Request> requestType();

    /**
     * Returns the response type.
     */
    Class<? extends Response> responseType();

}
