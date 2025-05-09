package io.effi.rpc.contract.repository;

import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.collection.LazyList;
import io.effi.rpc.contract.module.EffiRpcModule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Handles shared components of type {@link T},
 * Registering and accessing shared components in a module context.
 */
public abstract class SharableComponentRepository<T> extends AbstractComponentRepository<T> {

    protected List<T> sharedComponents = new LazyList<>(ArrayList::new);

    protected SharableComponentRepository(EffiRpcModule module) {
        super(module);
    }

    /**
     * Registers one or more shared components.
     *
     * @param components the components to be registered as shared.
     */
    @SuppressWarnings("unchecked")
    public void registerShared(T... components) {
        if (CollectionUtil.isNotEmpty(components)) {
            CollectionUtil.addUnique(sharedComponents, components);
        }
    }

    public List<T> sharedComponents() {
        return Collections.unmodifiableList(sharedComponents);
    }
}

