package io.effi.rpc.util;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Provides thread-safe lazy initialization with guaranteed singleton behavior.
 * <p>
 * Defers instance creation until first access, ensuring it is created only once.
 * Supports breaking recursive initialization cycles by exposing a pre-created
 * instance if necessary.
 */
public class LazySingleton<T> {

    private final Function<LazySingleton<T>, T> creator;

    protected volatile T instance;

    private LazySingleton(Function<LazySingleton<T>, T> creator) {
        this.creator = creator;
    }

    /**
     * Creates a lazy singleton initialized by the supplied supplier.
     *
     * @param creator instance supplier
     * @param <T> instance type
     * @return lazy singleton
     */
    public static <T> LazySingleton<T> from(Supplier<T> creator) {
        AssertUtil.notNull(creator, "creator");
        return new LazySingleton<>(lazy -> creator.get());
    }

    /**
     * Creates a lazy singleton initialized by the supplied factory.
     *
     * @param creator instance factory
     * @param <T> instance type
     * @return lazy singleton
     */
    public static <T> LazySingleton<T> from(Function<LazySingleton<T>, T> creator) {
        AssertUtil.notNull(creator, "creator");
        return new LazySingleton<>(creator);
    }

    /**
     * Exposes a pre-created instance to be used if initialization hasn't yet occurred.
     * <p>
     * This is useful for breaking cycles during recursive or reentrant initialization.
     *
     * @param value pre-created instance
     */
    public void expose(T value) {
        if (value == null) return;
        T current = instance;
        if (current == null) {
            synchronized (this) {
                if (instance == null) {
                    instance = value;
                }
            }
        }
    }

    /**
     * Returns the initialized instance, creating it on first access.
     *
     * @throws IllegalStateException if the creator returns {@code null}
     */
    public T ensure() {
        T reuslt = instance;
        if (reuslt != null) return reuslt;
        synchronized (this) {
            reuslt = instance;
            return reuslt != null ? reuslt : (instance = creator.apply(this));
        }
    }

    /**
     * Checks if the instance has been initialized.
     */
    public boolean initialized() {
        return instance != null;
    }

    @Override
    public String toString() {
        T value = instance;
        String name = value != null
                ? ObjectUtil.simpleClassName(value)
                : "<uninitialized>";
        return "Lazy[" + name + "]";
    }

}



