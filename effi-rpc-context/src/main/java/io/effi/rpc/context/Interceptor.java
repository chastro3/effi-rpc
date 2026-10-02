package io.effi.rpc.context;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.trait.Ordered;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.MODULE;

/**
 * Intercepts RPC calls during call and reply phases.
 * <p>
 * Provides extensible interception capabilities for both client and server
 * sides of RPC interactions with support for call, chosen, and reply phases.
 * <p>
 * Client flow:<br>
 * {@link CallUnit} → {@link Locator} → {@link ChosenUnit} → Send Request<br>
 * Receive Response → {@link ReplyUnit}.
 * <p>
 * Server flow:<br>
 * {@link CallUnit} → Invoke Servant -> {@link ReplyUnit} → Send Response
 *
 * @see CallUnit
 * @see ChosenUnit
 * @see ReplyUnit
 */
@Extensible(scope = MODULE)
public interface Interceptor<M extends Message, P extends Peer, C extends Interaction.Context<M, P>>
        extends Interaction.Unit<M, P>, Ordered {

    /**
     * Intercepts the current context and proceeds the chain.
     *
     * @param context the current exchange context
     * @param chain   the interceptor chain
     * @return the result of processing
     */
    Interaction.Result intercept(C context, Chain chain);

    /**
     * Intercepts the {@link CallContext} during the call phase.
     * <p>
     * For the client, invoked before {@link Locator#locate(CallContext)}.<br>
     * For the server, invoked before {@link Servant#invoke(Object...)}.
     *
     * @see Interceptor
     * @see CallContext
     */
    interface CallUnit<R extends Request, P extends Peer> extends Interceptor<R, P, CallContext<R, P>> {

        @Override
        Interaction.Result intercept(CallContext<R, P> context, Chain chain);

    }

    /**
     * Intercepts the request after addressing, before sending.
     * <p>
     * Used only in the client, invoked after {@link Locator#locate(CallContext)}
     * has completed addressing and before sending the request.
     *
     * @see Interceptor
     * @see CallContext
     */
    interface ChosenUnit<R extends Request, C extends Caller<?>> extends Interceptor<R, C, CallContext<R, C>> {

        @Override
        Interaction.Result intercept(CallContext<R, C> context, Chain chain);
    }

    /**
     * Intercepts the {@link ReplyContext} during the reply phase.
     * <p>
     * For the client, invoked after a successful response is decoded and before returning it to the
     * caller; failed attempts carry no decoded reply and never reach this unit, they are owned by
     * {@link io.effi.rpc.context.support.Unary.FailureHandler} instead.<br>
     * For the server, invoked after processing the request and before sending the response, for both
     * succeeded and failed invocation results.
     *
     * @see Interceptor
     * @see ReplyContext
     */
    interface ReplyUnit<R extends Response, P extends Peer> extends Interceptor<R, P, ReplyContext<R, P>> {

        @Override
        Interaction.Result intercept(ReplyContext<R, P> context, Chain chain);
    }

    /**
     * Defines a chain of {@link Interceptor} execution units.
     */
    interface Chain extends Interaction.UnitChain {}

}
