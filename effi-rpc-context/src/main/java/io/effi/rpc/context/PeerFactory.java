package io.effi.rpc.context;

import io.effi.rpc.context.parameter.MethodMapper;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.config.HierarchicalConfig;
import io.effi.rpc.util.TypeCapture;

/**
 * Creates protocol-specific caller and callee instances.
 * <p>
 * Provides factory methods for creating RPC caller and callee implementations
 * based on method mappers, reply types, and configuration settings.
 */
public interface PeerFactory {

    /**
     * Creates a callee from the specified method mapper and configuration.
     *
     * @param methodMapper the method mapper defining the exposed methods
     * @param config the callee configuration
     * @param module the associated module
     * @return the created callee
     */
    <T> Callee createCallee(MethodMapper<T> methodMapper, HierarchicalConfig config, ScopedModule module);

    /**
     * Creates a caller for the specified reply type, configuration.
     *
     * @param returnType the expected reply type
     * @param config the caller configuration
     * @param module the associated module
     * @return the created caller
     */
    <T> Caller<T> createCaller(TypeCapture<T> replyType, HierarchicalConfig config, ScopedModule module);
}

