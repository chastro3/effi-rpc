package io.effi.rpc.contract.filter;

import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.Locator;
import io.effi.rpc.contract.context.InvocationContext;

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


