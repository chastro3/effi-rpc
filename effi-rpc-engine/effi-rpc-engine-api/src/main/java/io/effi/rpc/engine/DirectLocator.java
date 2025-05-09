package io.effi.rpc.engine;

import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.Locator;
import io.effi.rpc.contract.context.InvocationContext;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.NetUtil;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves the remote address directly, returning the predefined {@link InetSocketAddress}.
 */
public class DirectLocator implements Locator {

    private static final Map<String, DirectLocator> RESOURCES = new ConcurrentHashMap<>();

    private final InetSocketAddress remoteAddress;

    private DirectLocator(InetSocketAddress remoteAddress) {
        this.remoteAddress = AssertUtil.notNull(remoteAddress, "remote address");
    }

    public static DirectLocator getInstance(String address) {
        AssertUtil.notNull(address, "address");
        return RESOURCES.computeIfAbsent(address, k -> new DirectLocator(NetUtil.toInetSocketAddress(address)));
    }

    @Override
    public InetSocketAddress locate(InvocationContext<Envelope.Request, Caller<?>> context) {
        return remoteAddress;
    }
}

