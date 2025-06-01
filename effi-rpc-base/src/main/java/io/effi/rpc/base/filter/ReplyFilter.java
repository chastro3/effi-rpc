package io.effi.rpc.base.filter;

import io.effi.rpc.base.Envelope;
import io.effi.rpc.base.Invoker;
import io.effi.rpc.base.context.ReplyContext;

/**
 * Intercepts RPC response.
 * <p>
 * For the client, invoked after receiving the response and before returning it to the caller.
 * For the server, invoked after processing the request and before sending the response.
 * </p>
 *
 * @see Filter
 * @see ReplyContext
 */
@FunctionalInterface
public interface ReplyFilter<T extends Envelope.Response, I extends Invoker<?>> extends Filter<T, I, ReplyContext<T, I>> {}

