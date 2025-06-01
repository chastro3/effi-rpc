package io.effi.rpc.base.repository;

import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.GenericKey;
import io.effi.rpc.util.collection.LazyMap;
import io.effi.rpc.util.resoruce.Cleanable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Manages global resources.
 */
public final class GlobalResourceRepository implements Cleanable {

    private static final GlobalResourceRepository INSTANCE = new GlobalResourceRepository();

    private final Map<GenericKey<?>, Resource<?>> resources = new LazyMap<>(ConcurrentHashMap::new);

    private GlobalResourceRepository() {
    }

    public static <T> T compute(GenericKey<T> key, Supplier<T> creator, Consumer<T> closer) {
        return INSTANCE.computeIfAbsent(key, creator, closer);
    }

    @SuppressWarnings("unchecked")
    public <T> T computeIfAbsent(GenericKey<T> key, Supplier<T> creator, Consumer<T> closer) {
        Resource<T> resource = (Resource<T>) resources.computeIfAbsent(key,
                k -> new Resource<>(creator.get(), closer));
        return resource.value();
    }

    public static GlobalResourceRepository getInstance() {
        return INSTANCE;
    }

    @Override
    public void clear() {
        for (Resource<?> resource : resources.values()) {
            @SuppressWarnings("unchecked")
            Consumer<Object> closer = (Consumer<Object>) resource.closer();
            closer.accept(resource.value());
        }
    }

    private record Resource<T>(T value, Consumer<T> closer) {

        Resource(T value, Consumer<T> closer) {
            this.value = AssertUtil.notNull(value, "value");
            this.closer = AssertUtil.notNull(closer, "closer");
        }

    }

}
