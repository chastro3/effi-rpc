package io.effi.rpc.context.support;

import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.Interceptor;
import io.effi.rpc.util.CollectionUtil;

import java.util.Collections;
import java.util.Map;

/**
 * Provides an immutable chain of interceptor instances.
 * <p>
 * Implements an immutable chain structure for processing interceptors with
 * creation support for interceptor chains.
 *
 * @see ImmutableInteractionUnitChain
 * @see Interceptor.Chain
 */
@SuppressWarnings("rawtypes")
public final class ImmutableInterceptorChain extends ImmutableInteractionUnitChain<Interceptor, ImmutableInterceptorChain>
        implements Interceptor.Chain {

    private static final Interceptor TAIL_INTERCEPTOR = (ctx, chain) -> null;

    private static final ImmutableInterceptorChain TAIL = new ImmutableInterceptorChain("empty", TAIL_INTERCEPTOR, null);

    /**
     * Creates an interceptor chain for the given module and interceptor names.
     *
     * @param module the scoped module to lookup interceptors from
     * @param names  the interceptor names to include in the chain
     * @return the interceptor chain, or empty chain if names are empty
     */
    public static ImmutableInterceptorChain of(ScopedModule module, String[] names) {
        return of(module, names, Collections.emptyMap());
    }

    /**
     * Creates an interceptor chain with additional named interceptors.
     *
     * @param module               the scoped module to lookup interceptors from
     * @param names                the interceptor names to include in the chain
     * @param additionalInterceptors interceptors that are not registered as extensions
     * @return the interceptor chain, or empty chain if names are empty
     */
    public static ImmutableInterceptorChain of(ScopedModule module, String[] names,
                                               Map<String, Interceptor> additionalInterceptors) {
        if (CollectionUtil.isEmpty(names)) return TAIL;
        return init(module,
                (scopedModule, name) -> lookupInterceptor(scopedModule, name, additionalInterceptors),
                names,
                ImmutableInterceptorChain::new,
                TAIL);
    }

    /**
     * Initializes an interceptor chain from the given interceptor map.
     *
     * @param filters the map of interceptors to include in the chain
     * @return the initialized interceptor chain
     */
    public static ImmutableInterceptorChain init(Map<String, Interceptor> filters) {
        if (CollectionUtil.isEmpty(filters)) return TAIL;
        return init(filters, ImmutableInterceptorChain::new, TAIL);
    }

    private static Interceptor lookupInterceptor(ScopedModule module, String name,
                                                 Map<String, Interceptor> additionalInterceptors) {
        Interceptor interceptor = additionalInterceptors.get(name);
        if (interceptor != null) return interceptor;
        return module.namedExtension(Interceptor.class, name);
    }

    private ImmutableInterceptorChain(String name, Interceptor interceptor, ImmutableInterceptorChain next) {
        super(name, interceptor, next);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <C extends Interaction.Context> Interaction.Result proceed(C context) {
        return unit.intercept(context, next);
    }
}
