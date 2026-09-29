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

    public void set(int index, Object value) {
        values[index] = value;
    }

    @Override
    public String toString() {
        return Arrays.toString(values);
    }
}
