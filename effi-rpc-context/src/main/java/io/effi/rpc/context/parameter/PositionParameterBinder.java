package io.effi.rpc.context.parameter;

import io.effi.rpc.context.invocation.Invocation;

/**
 * Binds a parameter by its method position.
 */
public final class PositionParameterBinder implements ParameterBinder {

    public static final PositionParameterBinder INSTANCE = new PositionParameterBinder();

    private PositionParameterBinder() {
    }

    @Override
    public void bind(Object value, ParameterBinding binding, Invocation invocation) {
        invocation.arguments().set(binding.index(), value);
    }

    @Override
    public Object resolve(ParameterBinding binding, Invocation invocation) {
        int index = binding.index();
        if (index >= invocation.arguments().size()) {
            throw new IllegalArgumentException("Missing positional argument at index " + index);
        }
        return invocation.arguments().get(index);
    }
}
