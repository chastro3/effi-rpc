package io.effi.rpc.base.filter;

import io.effi.rpc.base.Callee;
import io.effi.rpc.base.Envelope;
import io.effi.rpc.base.Invoker;
import io.effi.rpc.base.Locator;
import io.effi.rpc.base.context.InvocationContext;

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

