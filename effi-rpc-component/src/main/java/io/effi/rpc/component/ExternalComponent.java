package io.effi.rpc.component;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.GenericKey;
import io.effi.rpc.util.Identifiable;
import io.effi.rpc.util.resoruce.Cleanable;

/**
 * Wraps external component instances with metadata and lifecycle management.
 * <p>
 * Provides a standardized way to manage non-scoped components with
 * associated metadata, tags, and optional cleanup capabilities.
 */
@ScopedComponent
public class ExternalComponent<T> extends DynamicTagComponent implements Identifiable, Cleanable {

    private final GenericKey<T> name;

    private final T value;

    private final Cleanable cleanable;

    public ExternalComponent(GenericKey<T> name, T value) {
        this(name, value, null);
    }

    public ExternalComponent(GenericKey<T> name, T value, Cleanable cleanable) {
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

    @Override
    public String toString() {
        return "ExternalComponent{name=" + name + ", value=" + value + "}";
    }
}
