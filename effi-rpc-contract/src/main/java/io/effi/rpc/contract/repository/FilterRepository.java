package io.effi.rpc.contract.repository;

import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.contract.filter.Filter;
import io.effi.rpc.contract.filter.FilterSupport;
import io.effi.rpc.contract.module.EffiRpcModule;

import java.util.Arrays;

/**
 * Manage the registration and retrieval of {@link Filter} instances.
 */
public class FilterRepository extends SharableComponentRepository<Filter<?, ?, ?>> {

    public FilterRepository(EffiRpcModule module) {
        super(module);
    }

    @Override
    public void register(String key, Filter<?, ?, ?> value) {
        if (FilterSupport.isSupport(value)) {
            super.register(key, value);
        }

    }

    @Override
    public void registerShared(Filter<?, ?, ?>... values) {
        if (CollectionUtil.isNotEmpty(values)) {
            Arrays.stream(values)
                    .filter(FilterSupport::isSupport).forEach(super::registerShared);
        }
    }
}
