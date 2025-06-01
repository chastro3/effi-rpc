package io.effi.rpc.base;

import io.effi.rpc.base.context.InvocationContext;

import java.net.InetSocketAddress;

/**
 * Locates target addresses based on the invocation context.
 */
public interface Locator {

    /**
     * Locates the target URL based on the invocation context.
     *
     * @param context the {@link InvocationContext} instance for the method call
     * @return the {@link InetSocketAddress} representing the target address, or {@code null} if no address is found
     */
    InetSocketAddress locate(InvocationContext<Envelope.Request, Caller<?>> context);
}


