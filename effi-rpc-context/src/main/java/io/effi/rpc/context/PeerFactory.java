package io.effi.rpc.context;

import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.config.HierarchicalOptions;
import io.effi.rpc.context.parameter.ServantMethod;
import io.effi.rpc.util.TypeCapture;

/**
 * Creates protocol-specific caller and servant instances.
 * <p>
 * Provides factory methods for creating RPC caller and servant implementations
 * based on method mappers, reply types, and configuration settings.
 */
public interface PeerFactory {

    /**
     * Creates a servant from the specified method mapper and configuration.
     *
     * @param servantMethod the method mapper defining the exposed methods
     * @param config the servant configuration
     * @param module the associated module
     * @return the created servant
     */
    <T> Servant createServant(ServantMethod<T> servantMethod, HierarchicalOptions options, ScopedModule module);

    /**
     * Creates a caller for the specified reply type, configuration.
     *
     * @param returnType the expected reply type
     * @param config the caller configuration
     * @param module the associated module
     * @return the created caller
     */
    <T> Caller<T> createCaller(TypeCapture<T> replyType, HierarchicalOptions options, ScopedModule module);
}

