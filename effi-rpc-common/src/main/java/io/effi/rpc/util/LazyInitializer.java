package io.effi.rpc.util;

import java.util.function.Supplier;

/**
 * Provides lazy initialization for instances.
 *
 * @param <T> the type of the lazily initialized object
 */
public class LazyInitializer<T> {

    private final Supplier<T> supplier;

    protected volatile T instance;

    public LazyInitializer(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    /**
     * Returns the instance, initializing it if necessary.
     */
    public T get(boolean canNull) {
        if (instance == null) {
            // Return null without initializing if allowed
            if (canNull) return instance;
            synchronized (this) {
                if (instance == null) {
                    instance = supplier.get();
                }
            }
        }
        return instance;
    }

    /**
     * Returns the instance or null if uninitialized.
     */
    public T get() {
        return get(true);
    }
}


