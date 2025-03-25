package io.effi.rpc.common.extension;

/**
 * A container that holds a value of type {@link T}.
 * Provides basic getter and setter methods to encapsulate and modify the held value.
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
     * Sets a new value of type {@link NEW} for this holder and returns the updated holder.
     * Allows changing the type of the held value.
     *
     * @param value the new value to set
     * @param <NEW> the type of the new value
     * @return the updated {@link Holder} containing the new value
     */
    public <NEW> Holder<NEW> set(NEW value) {
        this.value = value;
        return (Holder<NEW>) this;
    }

    /**
     * Retrieves the value held by this {@link Holder}.
     *
     * @return the value of type {@link T} held by this holder
     */
    public T get() {
        return (T) value;
    }
}


