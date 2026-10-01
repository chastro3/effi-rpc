package io.effi.rpc.component.event;

import io.effi.rpc.util.AssertUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Stores event handlers and resolves versioned handler chains.
 */
final class HandlerRegistry {

    private final CopyOnWriteArrayList<Registration> registrations = new CopyOnWriteArrayList<>();

    private final AtomicLong version = new AtomicLong();

    <E extends Event> void register(Class<E> eventType, EventHandler<E> handler) {
        AssertUtil.notNull(eventType, "eventType");
        AssertUtil.notNull(handler, "handler");
        registrations.add(new Registration(eventType, handler));
        version.incrementAndGet();
    }

    Chain resolve(Class<?> eventType, Cache cache) {
        long currentVersion = version.get();
        if (cache.eventType == eventType && cache.version == currentVersion) {
            return cache.chain;
        }
        Chain chain = cache.chains.get(eventType);
        if (chain != null && chain.version() == currentVersion) {
            cache.remember(eventType, chain);
            return chain;
        }
        List<EventHandler<?>> resolved = new ArrayList<>();
        for (Registration registration : registrations) {
            if (registration.eventType().isAssignableFrom(eventType)) {
                resolved.add(registration.handler());
            }
        }
        Chain computed = new Chain(currentVersion, resolved.toArray(EventHandler[]::new));
        cache.remember(eventType, computed);
        return computed;
    }

    record Chain(long version, EventHandler<?>[] handlers) {
    }

    static final class Cache {

        private final Map<Class<?>, Chain> chains = new HashMap<>();

        private Class<?> eventType;

        private Chain chain;

        private long version = -1L;

        private void remember(Class<?> eventType, Chain chain) {
            this.eventType = eventType;
            this.chain = chain;
            this.version = chain.version();
            chains.put(eventType, chain);
        }
    }

    private record Registration(Class<? extends Event> eventType, EventHandler<? extends Event> handler) {
    }
}
