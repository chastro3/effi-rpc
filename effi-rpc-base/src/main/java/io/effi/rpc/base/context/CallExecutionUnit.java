package io.effi.rpc.base.context;

import io.effi.rpc.base.CallSide;
import io.effi.rpc.base.Message;

/**
 * Defines an execution unit that handles the {@link CallContext}.
 */
public interface CallExecutionUnit<R extends Message.Request, S extends CallSide, CHAIN extends ExecutionUnitChain>
        extends ExecutionUnit<R, S, CallContext<R, S>, CHAIN> {}
