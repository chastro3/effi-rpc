package io.effi.rpc.component;

import io.effi.rpc.util.AssertUtil;

/**
 * Manages single component relationships within ScopedComponentRepository.
 */
public class SingleComponent<T> {

    private final ScopedComponentRepository owner;

    private final String key;

    private T component;

    SingleComponent(String key, T component, ScopedComponentRepository owner) {
        this.key = AssertUtil.notBlank(key, "key");
        this.component = AssertUtil.notNull(component, "component");
        this.owner = AssertUtil.notNull(owner, "owner");
    }

    public void component(T component) {
        this.component = component;
    }

    public String key() {
        return key;
    }

    public ScopedComponentRepository owner() {
        return owner;
    }

    public T component() {
        return component;
    }
}