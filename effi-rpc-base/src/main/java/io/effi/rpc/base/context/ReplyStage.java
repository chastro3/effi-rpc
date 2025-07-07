package io.effi.rpc.base.context;

import io.effi.rpc.base.Caller;
import io.effi.rpc.base.Message;

/**
 * Defines a stage that handles the {@link ReplyContext} during the reply phase.
 *
 * @see Stage
 * @see ReplyContext
 */
public interface ReplyStage<R extends Message.Response, C extends Caller<?>>
        extends Stage<R, C, ReplyContext<R, C>>, ReplyExecutionUnit<R, C, StageChain> {}
