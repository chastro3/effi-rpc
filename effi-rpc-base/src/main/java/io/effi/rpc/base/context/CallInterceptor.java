package io.effi.rpc.base.context;

import io.effi.rpc.base.CallSide;
import io.effi.rpc.base.Callee;
import io.effi.rpc.base.Locator;
import io.effi.rpc.base.Message;

/**
 * Intercepts the {@link CallContext} during the call phase.
 * <p>
 * For the client, invoked before {@link Locator#locate(CallContext)}.<br>
 * For the server, invoked before {@link Callee#invoke(Object...)}.
 * </p>
 *
 * @see Interceptor
 * @see CallContext
 */
public interface CallInterceptor<R extends Message.Request, S extends CallSide>
        extends Interceptor<R, S, CallContext<R, S>>, CallExecutionUnit<R, S, InterceptorChain> {}

