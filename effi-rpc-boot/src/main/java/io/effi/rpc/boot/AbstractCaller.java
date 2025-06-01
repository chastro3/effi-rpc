package io.effi.rpc.boot;

import io.effi.rpc.base.Caller;
import io.effi.rpc.base.CompletableReplyFuture;
import io.effi.rpc.base.Envelope;
import io.effi.rpc.base.Locator;
import io.effi.rpc.base.ReplyFuture;
import io.effi.rpc.base.ResultType;
import io.effi.rpc.base.context.InvocationContext;
import io.effi.rpc.base.filter.ChosenFilter;
import io.effi.rpc.base.filter.Filter;
import io.effi.rpc.base.filter.FilterChain;
import io.effi.rpc.base.filter.FilterType;
import io.effi.rpc.base.filter.InvokeFilter;
import io.effi.rpc.base.filter.ReplyFilter;
import io.effi.rpc.boot.builder.CallerBuilder;
import io.effi.rpc.component.MultiComponent;
import io.effi.rpc.component.TagComponent;
import io.effi.rpc.config.DefaultConfigKeys;
import io.effi.rpc.config.NodeConfig;
import io.effi.rpc.config.registry.RegistryConfig;
import io.effi.rpc.config.transport.ClientConfig;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.constant.Tags;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.governance.faulttolerance.DefaultFailureHandler;
import io.effi.rpc.metrics.CallerMetrics;
import io.effi.rpc.metrics.MetricsSupport;
import io.effi.rpc.transport.TransportSupport;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.Messages;
import io.effi.rpc.util.Ordered;
import io.effi.rpc.util.StringUtil;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Provides an abstract implementation of {@link Caller}.
 */
public abstract class AbstractCaller<R> extends AbstractInvoker<CompletableFuture<R>, CallerBuilder<?, ?>> implements Caller<R> {

    protected Locator locator;

    protected ClientConfig clientConfig;

    protected List<ChosenFilter<?, ?>> chosenFilters;

    protected List<RegistryConfig> registryConfigs;


    protected AbstractCaller(NodeConfig config, CallerBuilder<?, ?> builder) {
        super(config, builder);
    }

    @SuppressWarnings("unchecked")
    @Override
    public CompletableFuture<R> call(Object... args) throws EffiRpcException {
        return (CompletableFuture<R>) startCall(args, CompletableReplyFuture::new)
                .failureHandler(DefaultFailureHandler.getInstance())
                .completableFuture();
    }

    @SuppressWarnings("unchecked")
    @Override
    public R blockingCall(Object... args) throws EffiRpcException {
        return (R) startCall(args, CompletableReplyFuture::new)
                .failureHandler(DefaultFailureHandler.getInstance())
                .get();
    }

    @Override
    public CompletableFuture<R> invoke(Object... args) throws EffiRpcException {
        return call(args);
    }

    @Override
    public ClientConfig clientConfig() {
        return clientConfig;
    }

    @Override
    public Locator locator() {
        return locator;
    }

    @Override
    public List<RegistryConfig> registryConfigs() {
        return Collections.unmodifiableList(registryConfigs);
    }

    @Override
    public <T extends ReplyFuture> T callWithFuture(T future) throws EffiRpcException {
        return TransportSupport.sendRequest(protocol, doCall(future));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void addFilters(Collection<Filter> filters) {
        if (CollectionUtil.isNotEmpty(filters)) {
            Class<? extends Envelope.Request> supportedRequestType = protocol.supportedRequestType();
            Class<? extends Envelope.Response> supportedResponseType = protocol.supportedResponseType();
            List<InvokeFilter<?, ?>> addedInvokeFilters = new ArrayList<>();
            List<ChosenFilter<?, ?>> addedChosenFilters = new ArrayList<>();
            List<ReplyFilter<?, ?>> addedReplyFilters = new ArrayList<>();
            for (Filter<?, ?, ?> filter : filters) {
                FilterType<?, ?> type = FilterType.extract(filter);
                Class<? extends Envelope> envelopeType = type.envelopeType();
                if (type.invokerType().isAssignableFrom(getClass())) {
                    if (filter instanceof InvokeFilter<?, ?> invokeFilter) {
                        if (envelopeType.isAssignableFrom(supportedRequestType)) {
                            addedInvokeFilters.add(invokeFilter);
                        }
                    } else if (filter instanceof ChosenFilter<?, ?> chosenFilter) {
                        if (envelopeType.isAssignableFrom(supportedRequestType)) {
                            addedChosenFilters.add(chosenFilter);
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
            CollectionUtil.addUnique(chosenFilters, addedChosenFilters);
            CollectionUtil.addUnique(replyFilters, addedReplyFilters);
        }
    }

    @Override
    protected void initialize(NodeConfig config, CallerBuilder<?, ?> builder) {
        super.initialize(config, builder);
        this.chosenFilters = new ArrayList<>();
        this.registryConfigs = new ArrayList<>();
        this.returnType = builder.returnType();
        this.locator = checkLocator(builder);
        this.clientConfig = checkClientConfig(builder);
    }

    @Override
    protected void onInitialized(NodeConfig config, CallerBuilder<?, ?> builder) {
        super.onInitialized(config, builder);
        module.register(Caller.class, this);
        addConfiguredRegistryConfigs();
        set(KeyConstant.LAST_CALL_INDEX, new AtomicInteger(-1));
        set(CallerMetrics.GENERIC_KEY, new CallerMetrics());
        tryPreloadDiscoveries();
    }

    protected <T extends ReplyFuture> T startCall(Object[] args, Function<InvocationContext<Envelope.Request, Caller<?>>, T> futureCreator) {
        Envelope.Request request = protocol.createRequest(this, args);
        InvocationContext<Envelope.Request, Caller<?>> context = new InvocationContext<>(module, request, this, args);
        return callWithFuture(futureCreator.apply(context));
    }

    private <T extends ReplyFuture> T doCall(T future) {
        var context = future.context();
        MetricsSupport.recordStartTime(context);
        // Chain of nested invocations for address resolution and filter execution
        var rpcContext = context.executor(() -> {
            var filterContext = context.executor(() -> {
                InetSocketAddress remoteAddress = locator().locate(context);
                context.envelope().url().address(remoteAddress);
                var chosenContext = context.executor(() -> {
                    future.whenComplete(replyContext -> {
                        replyContext = replyContext.executor(replyContext::result);
                        FilterChain.execute(replyContext, Ordered.sort(this.replyFilters));
                    });
                    return ResultType.FUTURE.createResult(context.envelope().url(), future);
                });
                return FilterChain.execute(chosenContext, Ordered.sort(this.chosenFilters));
            });
            return FilterChain.execute(filterContext, Ordered.sort(this.invokeFilters));
        });
        rpcContext.execute();
        return future;
    }

    private ClientConfig checkClientConfig(CallerBuilder<?, ?> builder) {
        ClientConfig clientConfig = builder.clientConfig();
        if (clientConfig != null) return clientConfig;
        String clientConfigName = get(DefaultConfigKeys.CLIENT_CONFIG);
        String defaultClientConfigName = ClientConfig.defaultKey(protocol());
        MultiComponent<ClientConfig> clientConfigComponent = platform().getMultiComponent(ClientConfig.class);
        if (clientConfigComponent != null) {
            TagComponent<ClientConfig> component = null;
            if (StringUtil.isBlank(clientConfigName)) {
                component = clientConfigComponent.lookup(defaultClientConfigName);
            } else {
                component = clientConfigComponent.lookup(clientConfigName);
                if (component == null) {
                    component = clientConfigComponent.lookup(defaultClientConfigName);
                }
            }
            if (component != null) {
                clientConfig = component.component();
            }
            if (clientConfig == null) {
                throw new IllegalStateException("No client config available for '" + id() + "'");
            }
        }
        return clientConfig;
    }

    private Locator checkLocator(CallerBuilder<?, ?> builder) {
        Locator locator = builder.locator();
        if (locator != null) return locator;
        String address = config.get(DefaultConfigKeys.ADDRESS);
        if (StringUtil.isNotBlank(address)) {
            locator = DirectLocator.getInstance(address);
        } else {
            String remoteApplication = config.get(DefaultConfigKeys.REMOTE_APPLICATION);
            if (StringUtil.isNotBlank(remoteApplication)) {
                locator = RegistryLocator.getInstance(remoteApplication);
            }
        }
        if (locator == null) {
            throw new IllegalStateException("No locator available for '" + id() + "'");
        }
        return locator;
    }

    private void addConfiguredRegistryConfigs() {
        List<String> registryConfigNames = getCascaded(DefaultConfigKeys.REGISTRIES);
        Collection<RegistryConfig> platformRegistryConfigs = platform().listOf(RegistryConfig.class, Tags.CONSUMER, Tags.FORCE_ACTIVE);
        for (RegistryConfig registryConfig : platformRegistryConfigs) {
            CollectionUtil.addUnique(registryConfigs, registryConfig);
        }
        for (String registryConfigName : registryConfigNames) {
            RegistryConfig registryConfig = module.lookup(RegistryConfig.class, registryConfigName);
            if (registryConfig != null) CollectionUtil.addUnique(registryConfigs, registryConfig);
        }
    }

    private void tryPreloadDiscoveries() {
        if (locator instanceof RegistryLocator registryLocator) {
            registryLocator.preloadDiscoveries(this);
        }
    }

}
