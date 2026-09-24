package io.effi.rpc.context.support;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Locator;
import io.effi.rpc.context.LocatorResolver;
import io.effi.rpc.context.PeerDescriptor;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.options.CallerOptions;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.NetUtil;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.effi.rpc.context.support.DirectLocator.Resolver.NAME;

/**
 * Resolves the remote address directly, returning the predefined {@link InetSocketAddress}.
 */
public final class DirectLocator implements Locator {

    private static final Map<String, DirectLocator> CACHE = new ConcurrentHashMap<>();

    private final InetSocketAddress remoteAddress;

    private DirectLocator(InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public static DirectLocator cached(String address) {
        AssertUtil.notNull(address, "address");
        return CACHE.computeIfAbsent(address, k -> new DirectLocator(NetUtil.toInetSocketAddress(address)));
    }

    @Override
    public InetSocketAddress locate(CallContext<Request, Caller<?>> context) {
        return remoteAddress;
    }

    @Extension(value = NAME)
    public static class Resolver implements LocatorResolver {

        public static final String NAME = "direct";

        @Override
        public Locator resolve(PeerDescriptor descriptor, ScopedPlatform platform) {
            String endpoint = descriptor.options().option(CallerOptions.ENDPOINT);
            return DirectLocator.cached(endpoint);
        }
    }
}

