package io.effi.rpc.context;

import java.net.InetSocketAddress;

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
}


