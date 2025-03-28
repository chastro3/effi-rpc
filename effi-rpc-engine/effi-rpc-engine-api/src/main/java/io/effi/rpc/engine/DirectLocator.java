package io.effi.rpc.engine;

import io.effi.rpc.common.util.AssertUtil;
import io.effi.rpc.common.util.Messages;
import io.effi.rpc.common.util.NetUtil;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.Locator;
import io.effi.rpc.contract.context.InvocationContext;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves the remote address directly, returning the predefined {@link InetSocketAddress}.
 */
public class DirectLocator implements Locator {

    private static final Map<String, DirectLocator> RESOURCES = new ConcurrentHashMap<>();

    private final InetSocketAddress remoteAddress;

    /**
     * Initializes with a specified {@link InetSocketAddress}.
     *
     * @param remoteAddress The remote address to be returned by the {@link #locate(CallInvocation)} method.
     * @throws IllegalArgumentException if the remote address is null.
     */
    DirectLocator(InetSocketAddress remoteAddress) {
        this.remoteAddress = AssertUtil.notNull(remoteAddress, "remote address");
    }

    /**
     * Initializes with a remote address string, converting it to an {@link InetSocketAddress}.
     *
     * @param remoteAddress The remote address as a string (e.g., "127.0.0.1:8080").
     */
    public DirectLocator(String remoteAddress) {
        this.remoteAddress = NetUtil.toInetSocketAddress(remoteAddress);
    }

    /**
     * Gets or create a DirectLocator instance.
     *
     * @param socketAddress the remote address
     * @return the DirectLocator instance
     */
    public static DirectLocator getInstance(InetSocketAddress socketAddress) {
        if (socketAddress == null)
            throw new IllegalArgumentException(Messages.notNull("socketAddress"));
        return RESOURCES.computeIfAbsent(NetUtil.toAddress(socketAddress), k -> new DirectLocator(socketAddress));
    }

    /**
     * Returns the predefined remote address.
     *
     * @param context The invocation context.
     * @return The {@link InetSocketAddress} that was passed to the constructor.
     */
    @Override
    public InetSocketAddress locate(InvocationContext<Envelope.Request, Caller<?>> context) {
        return remoteAddress;
    }
}

