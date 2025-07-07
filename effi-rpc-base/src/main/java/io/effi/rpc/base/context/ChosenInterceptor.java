package io.effi.rpc.base.context;

import io.effi.rpc.base.Caller;
import io.effi.rpc.base.Locator;
import io.effi.rpc.base.Message;

/**
 * Intercepts the request after addressing, before sending.
 * <p>
 * Used only in the client, invoked after {@link Locator#locate(CallContext)}
 * has completed addressing and before sending the request.
 * </p>
 *
 * @see Interceptor
 * @see CallContext
 */
public interface ChosenInterceptor<R extends Message.Request, C extends Caller<?>>
        extends Interceptor<R, C, CallContext<R, C>>, CallExecutionUnit<R, C, InterceptorChain> {}


