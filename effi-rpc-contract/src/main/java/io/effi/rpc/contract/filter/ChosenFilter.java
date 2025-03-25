package io.effi.rpc.contract.filter;

import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.Locator;
import io.effi.rpc.contract.context.InvocationContext;

/**
 * Filter for handling the request after addressing, but before it is sent.
 * <p>
 * Used only in the client flow, invoked after {@link Locator#locate(InvocationContext)}
 * has completed addressing and before sending the request to the target.
 * </p>
 *
 * @param <T> the type of the request
 * @param <I> the type of the caller
 * @see Filter
 * @see InvocationContext
 */
@FunctionalInterface
public interface ChosenFilter<T extends Envelope.Request, I extends Caller<?>> extends Filter<T, I, InvocationContext<T, I>> {}


