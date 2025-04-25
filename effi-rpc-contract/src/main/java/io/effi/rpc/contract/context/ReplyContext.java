package io.effi.rpc.contract.context;

import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.Invoker;
import io.effi.rpc.contract.Result;

/**
 * Context for an RPC reply.
 *
 * @param <T> the type of the response
 * @param <I> the type of the invoker
 */
public class ReplyContext<T extends Envelope.Response, I extends Invoker<?>>
        extends ExecutorContext<T, I, ReplyContext<T, I>> {

    private final InvocationContext<?, I> invocationContext;

    private final Result result;

    public ReplyContext(InvocationContext<?, I> invocationContext, T response, Result result) {
        super(invocationContext.module(), response, invocationContext.invoker());
        this.invocationContext = invocationContext;
        this.result = AssertUtil.notNull(result, "result");
    }

    /**
     * Returns the associated invocation context.
     */
    public InvocationContext<?, I> invocationContext() {
        return invocationContext;
    }

    /**
     * Returns the result of the invocation.
     */
    public Result result() {
        return result;
    }
}
