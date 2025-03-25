package io.effi.rpc.contract.filter;

import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.Invoker;
import io.effi.rpc.contract.context.ReplyContext;

/**
 * Filter for handling the RPC response.
 * <p>
 * In the client flow, invoked after receiving the response and before returning it to the caller.
 * </p>
 * <p>
 * In the server flow, invoked after processing the request and before sending the response.
 * </p>
 *
 * @param <T> the type of the response
 * @param <I> the type of the invoker
 * @see Filter
 * @see ReplyContext
 */
@FunctionalInterface
public interface ReplyFilter<T extends Envelope.Response, I extends Invoker<?>> extends Filter<T, I, ReplyContext<T, I>> {}

