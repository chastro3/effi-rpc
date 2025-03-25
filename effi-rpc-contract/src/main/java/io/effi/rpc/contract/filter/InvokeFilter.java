package io.effi.rpc.contract.filter;

import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.Invoker;
import io.effi.rpc.contract.Locator;
import io.effi.rpc.contract.context.InvocationContext;

/**
 * Filter for handling RPC invocation.
 * <p>
 * In the client flow, invoked before {@link Locator#locate(InvocationContext)}.
 * </p>
 * <p>
 * In the server flow, invoked before {@link Callee#invoke(Object...)}.
 * </p>
 *
 * @param <T> the type of the request
 * @param <I> the type of the invoker
 * @see Filter
 * @see InvocationContext
 */
@FunctionalInterface
public interface InvokeFilter<T extends Envelope.Request, I extends Invoker<?>> extends Filter<T, I, InvocationContext<T, I>> {}

