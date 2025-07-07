package io.effi.rpc.base.context;


import io.effi.rpc.component.EffiRpcModule;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.StringArrayKey;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides an immutable chain of {@link Interceptor} instances.
 *
 * <p>Supports efficient caching and creation of interceptor chains by name arrays or maps.</p>
 *
 * @see ImmutableExecutionUnitChain
 * @see InterceptorChain
 */
@SuppressWarnings("rawtypes")
public final class ImmutableInterceptorChain extends ImmutableExecutionUnitChain<Interceptor, ImmutableInterceptorChain> implements InterceptorChain {

    private static final ImmutableInterceptorChain EMPTY_CHAIN = new ImmutableInterceptorChain("empty", null, null);

    private static final Map<StringArrayKey, ImmutableInterceptorChain> CACHE = new ConcurrentHashMap<>();

    public static ImmutableInterceptorChain of(EffiRpcModule module, String[] names) {
        if (CollectionUtil.isEmpty(names)) return EMPTY_CHAIN;
        StringArrayKey key = StringArrayKey.of(names);
        return CACHE.computeIfAbsent(key, k ->
                init(module, ImmutableInterceptorChain::getFilter, names, ImmutableInterceptorChain::new, EMPTY_CHAIN)
        );
    }

    private static Interceptor getFilter(EffiRpcModule module, String name) {
        if (name.startsWith(StageInterceptor.PREFIX)) {
            return StageInterceptor.getInstance(name);
        }
        return module.getExtension(Interceptor.class, name);
    }

    private ImmutableInterceptorChain(String name, Interceptor interceptor, ImmutableInterceptorChain next) {
        super(name, interceptor, next);
    }

    public static ImmutableInterceptorChain init(Map<String, Interceptor> filters) {
        if (CollectionUtil.isEmpty(filters)) return EMPTY_CHAIN;
        return init(filters, ImmutableInterceptorChain::new, EMPTY_CHAIN);
    }
}
