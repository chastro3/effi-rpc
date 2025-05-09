package io.effi.rpc.engine;

import io.effi.rpc.config.DefaultConfigKeys;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.Messages;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.Invoker;
import io.effi.rpc.contract.filter.*;
import io.effi.rpc.contract.repository.FilterRepository;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.transport.TransportSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Configures invoker-related modular components.
 */
public abstract class InvokerModularConfig<T extends Invoker<?>> {

    protected final EffiRpcModule module;

    protected final T invoker;

    protected final List<InvokeFilter<?, ?>> invokeFilters = new ArrayList<>();

    protected final List<ReplyFilter<?, ?>> replyFilters = new ArrayList<>();

    protected InvokerModularConfig(EffiRpcModule module, T invoker) {
        // todo filter 添加时机优化例如未start时都可以添加成功
        this.module = AssertUtil.notNull(module, "module");
        this.invoker = AssertUtil.notNull(invoker, "invoker");
    }

    public void addFilter(Filter<?, ?, ?>... filters) {
        if (CollectionUtil.isNotEmpty(filters)) {
            Class<? extends Envelope.Request> supportedRequestType = getSupportedRequestType(invoker);
            Class<? extends Envelope.Response> supportedResponseType = getSupportedResponseType(invoker);
            for (Filter<?, ?, ?> filter : filters) {
                FilterType<?, ?> type = FilterType.extract(filter);
                Class<? extends Envelope> envelopeType = type.envelopeType();
                if (type.invokerType().isAssignableFrom(invoker.getClass())) {
                    switch (filter) {
                        case InvokeFilter<?, ?> invokeFilter:
                            if (envelopeType.isAssignableFrom(supportedRequestType)) {
                                CollectionUtil.addUnique(invokeFilters, invokeFilter);
                            }
                            break;
                        case ReplyFilter<?, ?> replyFilter:
                            if (envelopeType.isAssignableFrom(supportedResponseType)) {
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

    public EffiRpcModule module() {
        return module;
    }

    public T invoker() {
        return invoker;
    }

    public List<ReplyFilter<?, ?>> replyFilters() {
        return replyFilters;
    }

    public List<InvokeFilter<?, ?>> invokeFilters() {
        return invokeFilters;
    }

    protected void addConfiguredFilters() {
        List<String> filterNames = invoker.config().getCascaded(DefaultConfigKeys.FILTERS);
        FilterRepository filterManager = module.filterRepository();
        for (Filter<?, ?, ?> filter : filterManager.sharedComponents()) {
            addFilter(filter);
        }
        for (String filterName : filterNames) {
            Filter<?, ?, ?> filter = filterManager.get(filterName);
            if (filter != null) addFilter(filter);
        }
    }

    protected Class<? extends Envelope.Request> getSupportedRequestType(Invoker<?> invoker) {
        if (invoker instanceof AbstractInvoker<?> abstractInvoker) {
            return abstractInvoker.protocolInstance().supportedRequestType();
        } else {
            return TransportSupport.getProtocol(invoker.protocol()).supportedRequestType();
        }
    }

    protected Class<? extends Envelope.Response> getSupportedResponseType(Invoker<?> invoker) {
        if (invoker instanceof AbstractInvoker<?> abstractInvoker) {
            return abstractInvoker.protocolInstance().supportedResponseType();
        } else {
            return TransportSupport.getProtocol(invoker.protocol()).supportedResponseType();
        }
    }
}

