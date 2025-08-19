package io.effi.rpc.context;

import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.support.ThreadPool;
import io.effi.rpc.config.HierarchicalConfig;
import io.effi.rpc.config.QueryPath;
import io.effi.rpc.util.Attributes;
import io.effi.rpc.util.Identifiable;
import io.effi.rpc.util.TypeCapture;

/**
 * Defines a peer that shares behaviors of caller and callee.
 * <p>
 * Provides a common interface for both calling and called sides of interactions,
 * including protocol, query path, thread pool, and execution chain management.
 *
 * @see Caller
 * @see Callee
 */
public interface Peer extends Attributes, Identifiable, Protocol.Supplier, HierarchicalConfig.Supplier, ScopedModule.Supplier {

    /**
     * Returns the query path for this side.
     */
    QueryPath queryPath();

    /**
     * Returns the reply type.
     */
    TypeCapture<?> replyType();

    /**
     * Returns the thread pool for execution.
     */
    ThreadPool threadPool();

    /**
     * Returns the stage chain for execution.
     */
    Stage.Chain callStageChain();

    /**
     * Returns the reply stage chain.
     */
    Stage.Chain replyStageChain();

    /**
     * Returns the interceptor chain for calls.
     */
    Interceptor.Chain callInterceptorChain();

    /**
     * Returns the reply interceptor chain.
     */
    Interceptor.Chain replyInterceptorChain();
}




