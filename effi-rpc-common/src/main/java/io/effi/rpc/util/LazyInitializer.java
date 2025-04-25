package io.effi.rpc.util;

import java.util.function.Supplier;

/**
 * A utility class for lazily initializing instances. The instance is created
 * only when it is first accessed, and the initialization is thread-safe.
 *
 * @param <T> the type of the lazily initialized object
 */
public class LazyInitializer<T> {

    // Supplier providing initialization logic for the instance
    private final Supplier<T> supplier;

    // Lazily initialized instance (volatile for thread safety)
    protected volatile T instance;

    /**
     * Constructor accepting a Supplier to initialize the instance.
     *
     * @param supplier the Supplier used for instance creation
     */
    public LazyInitializer(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    /**
     * Retrieves the lazily initialized instance. If the instance is not initialized
     * and {@code canNull} is true, null is returned without initializing the instance.
     *
     * @param canNull flag indicating whether null can be returned if the instance is not initialized
     * @return the lazily initialized instance, or null if {@code canNull} is true and the instance is not initialized
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
     * Retrieves the lazily initialized instance, allowing it to return null if uninitialized.
     *
     * @return the lazily initialized instance, or null if not initialized
     */
    public T get() {
        return get(true);
    }
}


