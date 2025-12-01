package io.effi.rpc.context;

import io.effi.rpc.annotation.component.Extensible;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.MODULE;

/**
 * Defines processing stages in the RPC execution chain.
 * <p>
 * Provides extensible stage functionality for handling message exchange
 * contexts in the RPC processing pipeline with module-scoped components.
 */
@Extensible(scope = MODULE)
public interface Stage<M extends Message, P extends Peer, C extends Interaction.Context<M, P>> extends Interaction.Unit<M, P> {

    /**
     * Processes the stage with the given {@link Interaction.Context} and {@link Chain}.
     *
     * @param context the {@link InteractionContext} to process
     * @param chain   the {@link Chain} for sequential processing
     * @return the result of the stage execution
     */
    Interaction.Result process(C context, Chain chain);

    /**
     * Defines a stage that handles the {@link CallContext} during the call phase.
     * <p>
     * Provide specialized stage functionality for processing {@link CallContext}
     * in the RPC execution pipeline.
     *
     * @see Stage
     * @see CallContext
     */
    interface CallUnit<R extends Request, P extends Peer> extends Stage<R, P, CallContext<R, P>> {

        Interaction.Result process(CallContext<R, P> context, Chain chain);
    }

    /**
     * Defines a stage that handles the {@link ReplyContext} during the reply phase.
     * <p>
     * Provide specialized stage functionality for processing {@link ReplyContext}
     * in the RPC execution pipeline.
     *
     * @see Stage
     * @see ReplyContext
     */
    interface ReplyUnit<R extends Response, C extends Peer> extends Stage<R, C, ReplyContext<R, C>> {}

    /**
     * Defines a chain of {@link Stage} execution units.
     * <p>
     * Provide module-scoped execution chains for sequential processing
     * of stage units in the RPC execution pipeline.
     */
    interface Chain extends Interaction.UnitChain {}

}

