package io.effi.rpc.context.invocation;

import java.util.Arrays;

/**
 * Stores ordered invocation arguments.
 */
public record InvocationArguments(Object[] values) {

    public InvocationArguments(int size) {
        this(new Object[size]);
    }

    /**
     * Returns the mutable backing array.
     * <p>
     * Interceptors may update this array through {@link #set(int, Object)} before
     * the request is encoded. Callers must not retain or resize the returned array.
     *
     * @return mutable backing array
     */
    @Override
    public Object[] values() {
        return values;
    }

    @Override
    public String toString() {
        return Arrays.toString(values);
    }

    /**
     * Returns the argument count.
     */
    public int size() {
        return values.length;
    }

    /**
     * Returns whether this argument list is empty.
     */
    public boolean isEmpty() {
        return values.length == 0;
    }

    /**
     * Returns the argument at the supplied index.
     *
     * @param index argument index
     * @return argument value
     */
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
}
