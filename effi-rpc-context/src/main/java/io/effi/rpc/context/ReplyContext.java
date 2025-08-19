package io.effi.rpc.context;

import io.effi.rpc.util.AssertUtil;

/**
 * Represents reply contexts for RPC interactions.
 * <p>
 * Encapsulates the context information available after response receipt
 * and parsing on the client side or after method invocation but before
 * response sending on the server side.
 *
 * @param <T> the response message type
 * @param <I> the call side type
 */
public class ReplyContext<T extends Response, I extends Peer> extends Interaction.Context<T, I> {

    private final CallContext<?, I> callContext;

    private final Interaction.Result result;

    public ReplyContext(CallContext<?, I> callContext, T response, Interaction.Result result) {
        super(callContext.module(), response, callContext.peer(), callContext.mode());
        this.result = AssertUtil.notNull(result, "result");
        this.callContext = callContext;
    }

    public CallContext<?, I> callContext() {
        return callContext;
    }

    public Interaction.Result result() {
        return result;
    }
}
