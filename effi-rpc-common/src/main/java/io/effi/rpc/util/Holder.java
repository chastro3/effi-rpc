package io.effi.rpc.util;

/**
 * Holds a value of type {@link T}.
 * Provides getter and setter methods to encapsulate and modify the held value.
 *
 * @param <T> the type of the value being held
 */
@SuppressWarnings("unchecked")
public class Holder<T> {

    private Object value;

    public Holder() {
    }

    public Holder(T value) {
        this.value = value;
    }

    /**
     * Sets a new value for this holder and returns the updated holder.
     * Supports changing the type of the held value.
     */
    public <NEW> Holder<NEW> set(NEW value) {
        this.value = value;
        return (Holder<NEW>) this;
    }

    /**
     * Retrieves the value held by this {@link Holder}.
     */
    public T get() {
        return (T) value;
    }
}


