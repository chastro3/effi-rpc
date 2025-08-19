package io.effi.rpc.context;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.NetUtil;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.effi.rpc.context.DirectLocator.Factory.NAME;

/**
 * Resolves the remote address directly, returning the predefined {@link InetSocketAddress}.
 */
public final class DirectLocator implements Locator {

    private static final Map<String, DirectLocator> CACHE = new ConcurrentHashMap<>();

    private final InetSocketAddress remoteAddress;

    private DirectLocator(InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public static DirectLocator from(String address) {
        AssertUtil.notNull(address, "address");
        return CACHE.computeIfAbsent(address, k -> new DirectLocator(NetUtil.toInetSocketAddress(address)));
    }

    @Override
    public InetSocketAddress locate(CallContext<Request, Caller<?>> context) {
        return remoteAddress;
    }

    @Extension(value = NAME)
    public static class Factory implements LocatorFactory {

        public static final String NAME = "direct";

        @Override
        public Locator fetch(String target, Caller<?> caller) {
            return DirectLocator.from(target);
        }
    }
}

