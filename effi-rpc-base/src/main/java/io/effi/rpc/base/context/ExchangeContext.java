package io.effi.rpc.base.context;

import io.effi.rpc.base.CallSide;
import io.effi.rpc.base.Callee;
import io.effi.rpc.base.Caller;
import io.effi.rpc.base.Message;
import io.effi.rpc.component.EffiRpcModule;
import io.effi.rpc.util.AbstractAttributes;

/**
 * Encapsulates RPC call context and necessary call parameters.
 *
 * @param <M> the message type
 * @param <S> the call side type
 * @see CallContext
 * @see ReplyContext
 */
public abstract class ExchangeContext<M extends Message, S extends CallSide>
        extends AbstractAttributes implements EffiRpcModule.Provider {

    private final EffiRpcModule module;

    private final S callSide;

    private final M message;

    protected ExchangeContext(EffiRpcModule module, M message, S callSide) {
        this.module = module;
        this.message = message;
        this.callSide = callSide;
    }

    @Override
    public EffiRpcModule module() {
        return module;
    }

    public M message() {
        return message;
    }

    public S callSide() {
        return callSide;
    }


    /**
     * Checks if the context is server-side.
     */
    public boolean isCalleeSide() {
        return callSide instanceof Callee;
    }

    /**
     * Checks if the context is client-side.
     */
    public boolean isCallerSide() {
        return callSide instanceof Caller<?>;
    }
}

