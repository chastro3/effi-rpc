package io.effi.rpc.boot;

import io.effi.rpc.base.Caller;
import io.effi.rpc.base.Locator;
import io.effi.rpc.base.Message;
import io.effi.rpc.base.context.CallContext;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.NetUtil;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves the remote address directly, returning the predefined {@link InetSocketAddress}.
 */
public final class DirectLocator implements Locator {

    private static final Map<String, DirectLocator> CACHE = new ConcurrentHashMap<>();

    private final InetSocketAddress remoteAddress;

    private DirectLocator(InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public static DirectLocator of(String address) {
        AssertUtil.notNull(address, "address");
        return CACHE.computeIfAbsent(address, k -> new DirectLocator(NetUtil.toInetSocketAddress(address)));
    }

    @Override
    public InetSocketAddress locate(CallContext<Message.Request, Caller<?>> context) {
        return remoteAddress;
    }
}

