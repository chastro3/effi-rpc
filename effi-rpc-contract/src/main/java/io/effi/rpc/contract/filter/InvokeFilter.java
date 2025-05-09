package io.effi.rpc.contract.filter;

import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.Invoker;
import io.effi.rpc.contract.Locator;
import io.effi.rpc.contract.context.InvocationContext;

/**
 * Intercepts RPC invocation.
 * <p>
 * For the client, invoked before {@link Locator#locate(InvocationContext)}.
 * For the server, invoked before {@link Callee#invoke(Object...)}.
 * </p>
 *
 * @see Filter
 * @see InvocationContext
 */
@FunctionalInterface
public interface InvokeFilter<T extends Envelope.Request, I extends Invoker<?>> extends Filter<T, I, InvocationContext<T, I>> {}

