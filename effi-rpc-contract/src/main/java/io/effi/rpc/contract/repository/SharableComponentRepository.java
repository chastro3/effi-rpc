package io.effi.rpc.contract.repository;

import io.effi.rpc.common.util.CollectionUtil;
import io.effi.rpc.common.util.collection.LazyList;
import io.effi.rpc.contract.module.EffiRpcModule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Handles shared components of type {@link T},
 * Registering and accessing shared components in a module context.
 *
 * @param <T> the type of values managed by this class
 */
public abstract class SharableComponentRepository<T> extends AbstractComponentRepository<T> {

    protected List<T> sharedComponents = new LazyList<>(ArrayList::new);

    protected SharableComponentRepository(EffiRpcModule module) {
        super(module);
    }

    /**
     * Registers one or more shared components. If a value is already present,
     * it will not be added again.
     *
     * @param components the components to be registered as shared.
     */
    @SuppressWarnings("unchecked")
    public void registerShared(T... components) {
        if (CollectionUtil.isNotEmpty(components)) {
            CollectionUtil.addUnique(sharedComponents, components);
        }
    }

    /**
     * Returns the list of shared components.
     */
    public List<T> sharedComponents() {
        return Collections.unmodifiableList(sharedComponents);
    }
}

