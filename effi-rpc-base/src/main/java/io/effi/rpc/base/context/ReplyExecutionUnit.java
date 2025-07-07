package io.effi.rpc.base.context;

import io.effi.rpc.base.CallSide;
import io.effi.rpc.base.Message;

/**
 * Defines an execution unit that handles the {@link ReplyContext}.
 */
public interface ReplyExecutionUnit<R extends Message.Response, S extends CallSide, CHAIN extends ExecutionUnitChain>
        extends ExecutionUnit<R, S, ReplyContext<R, S>, CHAIN> {}
