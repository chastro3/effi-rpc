package io.effi.rpc.component;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.GenericKey;
import io.effi.rpc.util.Identifiable;
import io.effi.rpc.util.resoruce.Cleanable;

/**
 * Wraps a component with an associated {@link GenericKey} and optional cleanup logic.
 */
@ScopedComponent
public class WrappedComponent<T> extends DynamicTagComponent implements Identifiable, Cleanable {

    private final GenericKey<T> name;

    private final T value;

    private final Cleanable cleanable;

    public WrappedComponent(GenericKey<T> name, T component) {
        this(name, component, null);
    }

    public WrappedComponent(GenericKey<T> name, T value, Cleanable cleanable) {
        this.name = AssertUtil.notNull(name, "name");
        this.value = AssertUtil.notNull(value, "value");
        this.cleanable = cleanable;
    }

    public GenericKey<T> name() {
        return name;
    }

    public T value() {
        return value;
    }

    @Override
    public void clear() {
        if (cleanable != null) {
            cleanable.clear();
        }
    }

    @Override
    public String id() {
        return name.name();
    }
}
