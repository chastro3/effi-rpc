package io.effi.rpc.base;

import io.effi.rpc.base.context.InterceptorChain;
import io.effi.rpc.base.context.StageChain;
import io.effi.rpc.component.EffiRpcModule;
import io.effi.rpc.config.NodeConfig;
import io.effi.rpc.config.QueryPath;
import io.effi.rpc.util.Attributes;
import io.effi.rpc.util.Identifiable;
import io.effi.rpc.util.TypeToken;

/**
 * Defines a callable side that shares behaviors of caller and callee.
 *
 * @see Caller
 * @see Callee
 */
public interface CallSide extends Attributes, Identifiable, NodeConfig.Provider, EffiRpcModule.Provider {

    /**
     * Returns the call protocol.
     */
    String protocol();

    /**
     * Returns the query path for this side.
     */
    QueryPath queryPath();

    /**
     * Returns the thread pool for execution.
     */
    ThreadPool threadPool();

    /**
     * Returns the reply type.
     */
    TypeToken<?> replyType();

    /**
     * Returns the stage chain for execution.
     */
    StageChain callStageChain();

    /**
     * Returns the interceptor chain for calls.
     */
    InterceptorChain callInterceptorChain();
}




