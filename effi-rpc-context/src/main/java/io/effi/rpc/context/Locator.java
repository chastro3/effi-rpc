package io.effi.rpc.context;

import io.effi.rpc.annotation.component.Extensible;

import java.net.InetSocketAddress;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Locates target addresses based on invocation contexts.
 * <p>
 * Provides address resolution capabilities for RPC calls by determining
 * target endpoints from call context information.
 */
public interface Locator {

    /**
     * Locates the target URL based on the invocation context.
     *
     * @param context the {@link CallContext} instance for the method call
     * @return the {@link InetSocketAddress} representing the target address
     */
    InetSocketAddress locate(CallContext<Request, Caller<?>> context);

    /**
     * Creates locators for resolving target addresses in RPC calls.
     * <p>
     * Provides a factory interface for creating locator instances
     * based on target strings.
     */
    @Extensible(scope = PLATFORM)
    interface Factory {

        /**
         * Fetches a locator for the specified endpoint and caller.
         *
         * @param endpoint the endpoint address or identifier
         * @param caller the caller instance
         * @return the locator for resolving the target address
         */
        Locator fetch(String endpoint, Caller<?> caller);
    }
}


