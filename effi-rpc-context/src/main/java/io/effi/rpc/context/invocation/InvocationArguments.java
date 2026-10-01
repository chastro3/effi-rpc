package io.effi.rpc.context.invocation;

import java.util.Arrays;

/**
 * Stores ordered invocation arguments.
 */
public final class InvocationArguments {

    private final Object[] values;

    public InvocationArguments(int size) {
        this.values = new Object[size];
    }

    public InvocationArguments(Object[] values) {
        this.values = values;
    }

    /**
     * Returns the mutable backing array.
     * <p>
     * Interceptors may update this array through {@link #set(int, Object)} before
     * the request is encoded. Callers must not retain or resize the returned array.
     *
     * @return mutable backing array
     */
    public Object[] values() {
        return values;
    }

    public int size() {
        return values.length;
    }

    public boolean isEmpty() {
        return values.length == 0;
    }

    public Object get(int index) {
        return values[index];
    }

    /**
     * Replaces one positional argument.
     *
     * @param index argument index
     * @param value argument value
     */
    public void set(int index, Object value) {
        values[index] = value;
    }

    @Override
    public String toString() {
        return Arrays.toString(values);
    }
}
