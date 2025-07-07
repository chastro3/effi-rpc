package io.effi.rpc.base.context;

import io.effi.rpc.base.CallSide;
import io.effi.rpc.base.Message;
import io.effi.rpc.base.Result;
import io.effi.rpc.util.AssertUtil;

/**
 * Represents the reply context.
 * <p>
 * For the client, occurs after the response is received and parsed, but before the result is returned.<br>
 * For the server, occurs after the target method is invoked, but before the response is sent.
 * </p>
 *
 * @param <T> the response message type
 * @param <I> the call side type
 */
public class ReplyContext<T extends Message.Response, I extends CallSide> extends ExchangeContext<T, I> {

    private final CallContext<?, I> callContext;

    private final Result result;

    public ReplyContext(CallContext<?, I> callContext, T response, Result result) {
        super(callContext.module(), response, callContext.callSide());
        this.result = AssertUtil.notNull(result, "result");
        this.callContext = callContext;
    }

    public CallContext<?, I> callContext() {
        return callContext;
    }

    public Result result() {
        return result;
    }
}
