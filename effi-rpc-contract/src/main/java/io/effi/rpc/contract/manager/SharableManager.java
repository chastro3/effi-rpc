package io.effi.rpc.contract.manager;

import io.effi.rpc.common.util.collection.LazyList;
import io.effi.rpc.common.util.CollectionUtil;
import io.effi.rpc.contract.module.EffiRpcModule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Handles shared values of type {@link T},
 * Registering and accessing shared values in a module context.
 *
 * @param <T> the type of values managed by this class
 */
public class SharableManager<T> extends AbstractComponentManager<T> {

    protected List<T> sharedValues = new LazyList<>(ArrayList::new);

    protected SharableManager(EffiRpcModule module) {
        super(module);
    }

    /**
     * Registers one or more shared values. If a value is already present,
     * it will not be added again.
     *
     * @param values the values to be registered as shared.
     */
    @SuppressWarnings("unchecked")
    public void registerShared(T... values) {
        if (CollectionUtil.isNotEmpty(values)) {
            CollectionUtil.addUnique(sharedValues, values);
        }
    }

    /**
     * Returns the list of shared values.
     *
     * @return a {@link List} of shared values managed by this manager
     */
    public List<T> sharedValues() {
        return Collections.unmodifiableList(sharedValues);
    }
}

