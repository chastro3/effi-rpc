package io.effi.rpc.contract.context;

import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.Invoker;
import io.effi.rpc.contract.Result;
import io.effi.rpc.util.AssertUtil;

/**
 * Represents the context for an RPC reply.
 * <p>
 * For the client, occurs after the response is received and parsed,but before the result is returned.
 * For the server, occurs after the target method is invoked,but before the response is sent.
 * </p>
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

    public InvocationContext<?, I> invocationContext() {
        return invocationContext;
    }

    public Result result() {
        return result;
    }
}
