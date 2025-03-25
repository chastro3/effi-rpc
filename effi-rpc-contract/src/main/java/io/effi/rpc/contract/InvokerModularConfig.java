package io.effi.rpc.contract;

import io.effi.rpc.common.constant.DefaultConfigKeys;
import io.effi.rpc.common.util.AssertUtil;
import io.effi.rpc.common.util.CollectionUtil;
import io.effi.rpc.common.util.Messages;
import io.effi.rpc.contract.filter.Filter;
import io.effi.rpc.contract.filter.FilterSupport;
import io.effi.rpc.contract.filter.InvokeFilter;
import io.effi.rpc.contract.filter.ReplyFilter;
import io.effi.rpc.contract.manager.FilterManager;
import io.effi.rpc.contract.module.EffiRpcModule;

import java.util.ArrayList;
import java.util.List;

/**
 * Configures invoker-related modular components.
 *
 * @param <T> the type of the invoker
 */
public abstract class InvokerModularConfig<T extends Invoker<?>> {

    protected final EffiRpcModule module;

    protected final T invoker;

    protected final List<InvokeFilter<?, ?>> invokeFilters = new ArrayList<>();

    protected final List<ReplyFilter<?, ?>> replyFilters = new ArrayList<>();

    protected InvokerModularConfig(EffiRpcModule module, T invoker) {
        this.module = AssertUtil.notNull(module, "module");
        this.invoker = AssertUtil.notNull(invoker, "invoker");
    }

    /**
     * Adds filters to the configuration.
     * Filters are added based on invoker type and envelope type.
     *
     * @param filters the filters to add
     */
    public void addFilter(Filter<?, ?, ?>... filters) {
        if (CollectionUtil.isNotEmpty(filters)) {
            for (Filter<?, ?, ?> filter : filters) {
                FilterSupport.Type type = FilterSupport.getType(filter);
                Class<? extends Envelope> envelopeType = type.envelopeType();
                if (type.invokerType().isAssignableFrom(invoker.getClass())) {
                    switch (filter) {
                        case InvokeFilter<?, ?> invokeFilter:
                            if (envelopeType.isAssignableFrom(invoker.supportedRequestType())) {
                                CollectionUtil.addUnique(invokeFilters, invokeFilter);
                            }
                            break;
                        case ReplyFilter<?, ?> replyFilter:
                            if (envelopeType.isAssignableFrom(invoker.supportedResponseType())) {
                                CollectionUtil.addUnique(replyFilters, replyFilter);
                            }
                            break;
                        default:
                            throw new IllegalArgumentException(Messages.unSupport("filter", filter.getClass()));
                    }
                }
            }
        }
    }

    /**
     * Returns the module.
     */
    public EffiRpcModule module() {
        return module;
    }

    /**
     * Returns the invoker.
     */
    public T invoker() {
        return invoker;
    }

    /**
     * Returns the list of reply filters.
     */
    public List<ReplyFilter<?, ?>> replyFilters() {
        return replyFilters;
    }

    /**
     * Returns the list of invoke filters.
     */
    public List<InvokeFilter<?, ?>> invokeFilters() {
        return invokeFilters;
    }

    /**
     * Adds filters based on the invoker's configuration.
     */
    protected void addConfiguredFilters() {
        List<String> filterNames = invoker.getMerged(DefaultConfigKeys.FILTERS);
        FilterManager filterManager = module.filterManager();
        for (Filter<?, ?, ?> filter : filterManager.sharedValues()) {
            addFilter(filter);
        }
        for (String filterName : filterNames) {
            Filter<?, ?, ?> filter = filterManager.get(filterName);
            if (filter != null) addFilter(filter);
        }
    }
}

