package io.effi.rpc.base.context;

import io.effi.rpc.base.CallSide;
import io.effi.rpc.base.Message;

/**
 * Defines a stage that handles the {@link CallContext} during the call phase.
 *
 * @see Stage
 * @see CallContext
 */
public interface CallStage<R extends Message.Request, S extends CallSide>
        extends Stage<R, S, CallContext<R, S>>, CallExecutionUnit<R, S, StageChain> {}
