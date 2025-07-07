package io.effi.rpc.base;

import io.effi.rpc.base.context.CallContext;

import java.net.InetSocketAddress;

/**
 * Locates target addresses based on the invocation context.
 */
public interface Locator {

    /**
     * Locates the target URL based on the invocation context.
     *
     * @param context the {@link CallContext} instance for the method call
     * @return the {@link InetSocketAddress} representing the target address, or {@code null} if no address is found
     */
    InetSocketAddress locate(CallContext<Message.Request, Caller<?>> context);
}


