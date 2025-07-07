package io.effi.rpc.base.context;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.base.CallSide;
import io.effi.rpc.base.Message;
import io.effi.rpc.base.Result;
import io.effi.rpc.util.Ordered;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.MODULE;

/**
 * Intercepts RPC calls during call and reply phases.
 *
 * <p>
 * Client flow:<br>
 * CallInterceptor → Locator → ChosenInterceptor → Send Request<br>
 * Receive Response → ReplyInterceptor.
 * </p>
 * <p>
 * Server flow:<br>
 * CallInterceptor → Invoke Callee.
 * </p>
 *
 * @see CallInterceptor
 * @see ChosenInterceptor
 * @see ReplyInterceptor
 */
@Extensible(scope = MODULE)
public interface Interceptor<M extends Message, S extends CallSide, C extends ExchangeContext<M, S>>
        extends ExecutionUnit<M, S, C, InterceptorChain>, Ordered {


    @Override
    default Result execute(C context, InterceptorChain chain) {
        return intercept(context, chain);
    }

    /**
     * Intercepts the current context and proceeds the chain.
     *
     * @param context the current exchange context
     * @param chain   the interceptor chain
     * @return the result of processing
     */
    Result intercept(C context, InterceptorChain chain);

}