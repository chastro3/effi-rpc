package io.effi.rpc.base.filter;

import io.effi.rpc.base.Caller;
import io.effi.rpc.base.Envelope;
import io.effi.rpc.base.Locator;
import io.effi.rpc.base.context.InvocationContext;

/**
 * Intercepts the request after addressing, before sending.
 * <p>
 * Used only in the client, invoked after {@link Locator#locate(InvocationContext)}
 * has completed addressing and before sending the request.
 * </p>
 *
 * @see Filter
 * @see InvocationContext
 */
@FunctionalInterface
public interface ChosenFilter<T extends Envelope.Request, I extends Caller<?>> extends Filter<T, I, InvocationContext<T, I>> {}


