package io.effi.rpc.boot;

import io.effi.rpc.base.Callee;
import io.effi.rpc.base.Caller;
import io.effi.rpc.base.Envelope;
import io.effi.rpc.base.Invoker;
import io.effi.rpc.base.InvokerContainer;
import io.effi.rpc.base.ThreadPool;
import io.effi.rpc.base.filter.Filter;
import io.effi.rpc.base.filter.FilterType;
import io.effi.rpc.base.filter.InvokeFilter;
import io.effi.rpc.base.filter.ReplyFilter;
import io.effi.rpc.boot.builder.InvokerBuilder;
import io.effi.rpc.component.EffiRpcModule;
import io.effi.rpc.component.EffiRpcPlatform;
import io.effi.rpc.component.MultiComponent;
import io.effi.rpc.component.TagComponent;
import io.effi.rpc.config.Config;
import io.effi.rpc.config.ConfigKey;
import io.effi.rpc.config.DefaultConfigKeys;
import io.effi.rpc.config.NodeConfig;
import io.effi.rpc.config.QueryPath;
import io.effi.rpc.transport.Protocol;
import io.effi.rpc.transport.TransportSupport;
import io.effi.rpc.util.AbstractAttributes;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.Messages;
import io.effi.rpc.util.TypeToken;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Provides an abstract implementation of {@link Invoker}.
 */
public abstract class AbstractInvoker<R, B extends InvokerBuilder<?, ?>> extends AbstractAttributes implements Invoker<R> {

    protected String id;

    protected NodeConfig config;

    protected QueryPath queryPath;

    protected TypeToken<?> returnType;

    protected Protocol protocol;

    protected ThreadPool threadPool;

    protected EffiRpcModule module;

    protected List<InvokeFilter<?, ?>> invokeFilters;

    protected List<ReplyFilter<?, ?>> replyFilters;

    protected AbstractInvoker(NodeConfig config, B builder) {
        initialize(config, builder);
        onInitialized(config, builder);
    }

    @Override
    public NodeConfig config() {
        return config;
    }

    @Override
    public QueryPath queryPath() {
        return queryPath;
    }

    @Override
    public TypeToken<?> returnType() {
        return returnType;
    }

    @Override
    public String protocol() {
        return protocol.protocol();
    }

    @Override
    public EffiRpcModule module() {
        return module;
    }

    @Override
    public ThreadPool threadPool() {
        return threadPool;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String toString() {
        return queryPath.toString();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void addFilters(Collection<Filter> filters) {
        if (CollectionUtil.isNotEmpty(filters)) {
            Class<? extends Envelope.Request> supportedRequestType = protocol.supportedRequestType();
            Class<? extends Envelope.Response> supportedResponseType = protocol.supportedResponseType();
            List<InvokeFilter<?, ?>> addedInvokeFilters = new ArrayList<>();
            List<ReplyFilter<?, ?>> addedReplyFilters = new ArrayList<>();
            for (Filter<?, ?, ?> filter : filters) {
                FilterType<?, ?> type = FilterType.extract(filter);
                Class<? extends Envelope> envelopeType = type.envelopeType();
                if (type.invokerType().isAssignableFrom(getClass())) {
                    if (filter instanceof InvokeFilter<?, ?> invokeFilter) {
                        if (envelopeType.isAssignableFrom(supportedRequestType)) {
                            addedInvokeFilters.add(invokeFilter);
                        }
                    } else if (filter instanceof ReplyFilter<?, ?> replyFilter) {
                        if (envelopeType.isAssignableFrom(supportedResponseType)) {
                            addedReplyFilters.add(replyFilter);
                        }
                    } else {
                        throw new IllegalArgumentException(Messages.unSupport("filter", filter.getClass()));
                    }
                }
            }
            CollectionUtil.addUnique(invokeFilters, addedInvokeFilters);
            CollectionUtil.addUnique(replyFilters, addedReplyFilters);
        }
    }

    protected void initialize(NodeConfig config, B builder) {
        this.invokeFilters = new ArrayList<>();
        this.replyFilters = new ArrayList<>();
        this.config = AssertUtil.notNull(config, "config");
        this.module = AssertUtil.notNull(builder.module(), "module");
        this.queryPath = checkQueryPath(config);
        this.threadPool = checkThreadPool(builder);
        this.returnType = builder.returnType();
        this.protocol = TransportSupport.getProtocol(builder.protocol());
        this.id = InvokerContainer.invokerKey(protocol(), queryPath.path());
    }

    protected void onInitialized(NodeConfig config, B builder) {
        addConfiguredFilters(builder);
    }

    @SuppressWarnings("rawtypes")
    protected void addConfiguredFilters(B builder) {
        // todo 排除filter
        ArrayList<Filter> addedFilters = new ArrayList<>(builder.filters());
        List<String> filterNames = getCascaded(DefaultConfigKeys.FILTERS);
        EffiRpcPlatform platform = platform();
        MultiComponent<Filter> filterComponent = platform.getMultiComponent(Filter.class);
        addedFilters.addAll(filterComponent.listValueOf());
        for (String filterName : filterNames) {
            TagComponent<Filter> component = filterComponent.lookup(filterName);
            if (component != null) addedFilters.add(component.component());
        }
        addFilters(addedFilters);
    }

    private QueryPath checkQueryPath(Config config) {
        String path = config.get(DefaultConfigKeys.PATH);
        return path == null
                ? QueryPath.empty()
                : QueryPath.valueOf(path);
    }

    private ThreadPool checkThreadPool(B builder) {
        ThreadPool threadPool = builder.threadPool();
        if (threadPool != null) return threadPool;
        ConfigKey key = null;
        if (this instanceof Caller<?>) {
            key = DefaultConfigKeys.CALLER_THREAD_POOL;
        } else if (this instanceof Callee<?>) {
            key = DefaultConfigKeys.CALLEE_THREAD_POOL;
        }
        String name = get(key);
        threadPool = platform().lookup(ThreadPool.class, name);
        if (threadPool == null) {
            throw new IllegalStateException("No thread pool available for '" + id() + "'");
        }
        return threadPool;
    }
}
