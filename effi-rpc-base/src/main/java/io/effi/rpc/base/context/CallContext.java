package io.effi.rpc.base.context;

import io.effi.rpc.base.CallSide;
import io.effi.rpc.base.Message;
import io.effi.rpc.component.EffiRpcModule;

/**
 * Represents the call context.
 * <p>
 * For the client, occurs before the request is sent.<br>
 * For the server, occurs before the target method is invoked.
 * </p>
 *
 * @param <R> the request message type
 * @param <S> the call side type
 */
public class CallContext<R extends Message.Request, S extends CallSide> extends ExchangeContext<R, S> {

    private final Object[] args;

    public CallContext(EffiRpcModule module, R request, S callSide, Object[] args) {
        super(module, request, callSide);
        this.args = args;
    }

    public Object[] args() {
        return args;
    }
}


