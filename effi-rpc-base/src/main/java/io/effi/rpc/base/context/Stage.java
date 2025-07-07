package io.effi.rpc.base.context;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.base.CallSide;
import io.effi.rpc.base.Message;
import io.effi.rpc.base.Result;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.MODULE;

/**
 * Defines a processing stage in the execution chain.
 */
@Extensible(scope = MODULE)
public interface Stage<M extends Message, S extends CallSide, C extends ExchangeContext<M, S>>
        extends ExecutionUnit<M, S, C, StageChain> {

    @Override
    Result execute(C context, StageChain chain);

}

