package io.effi.rpc.context.support;

import io.effi.rpc.component.transport.ClientConfig;
import io.effi.rpc.component.transport.support.DefaultClientConfig;
import io.effi.rpc.concurrent.Future;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.CallerGroup;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.Interceptor;
import io.effi.rpc.context.InterceptorChainResolver;
import io.effi.rpc.context.Locator;
import io.effi.rpc.context.LocatorResolver;
import io.effi.rpc.context.Peer;
import io.effi.rpc.context.PeerDescriptor;
import io.effi.rpc.context.ReplyFuture;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.Stage;
import io.effi.rpc.context.metrics.CallerMetrics;
import io.effi.rpc.context.metrics.MetricsSupport;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.StringUtil;
import io.effi.rpc.util.TypeCapture;

import java.util.concurrent.atomic.AtomicInteger;

import static io.effi.rpc.context.options.CallerOptions.CLIENT;
import static io.effi.rpc.context.options.CallerOptions.ENDPOINT;
import static io.effi.rpc.context.options.CallerOptions.TIMEOUT;
import static io.effi.rpc.context.options.FaultToleranceOptions.FAILURE_HANDLER;
import static io.effi.rpc.context.options.GovernanceOptions.LOAD_BALANCER;
import static io.effi.rpc.context.options.GovernanceOptions.LOCATOR;
import static io.effi.rpc.context.options.GovernanceOptions.REGISTRY;
import static io.effi.rpc.context.options.GovernanceOptions.SERVICE_DISCOVERY_TIMEOUT;
import static io.effi.rpc.context.options.ResolverOptions.INTERCEPTOR_CHAIN_RESOLVER;

/**
 * Provides an immutable implementation of {@link Caller}.
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractCaller<R> extends AbstractPeer<AbstractCaller.Builder> implements Caller<R> {

    protected final Locator locator;

    protected final Unary.FailureHandler failureHandler;

    protected final ClientConfig clientConfig;

    protected final Interceptor.Chain chosenInterceptorChain;

    protected AbstractCaller(Builder builder) {
        super(builder);
        this.locator = builder.locator;
        this.failureHandler = builder.failureHandler;
        this.clientConfig = builder.clientConfig;
        this.chosenInterceptorChain = builder.chosenInterceptorChain;
        set(KeyConstant.LAST_CALL_INDEX, new AtomicInteger(-1));
        set(CallerMetrics.GENERIC_KEY, new CallerMetrics());
    }

    @Override
    public Future<R> call(Object... args) throws EffiRpcException {
        return doCall(args, Unary.MODE)
                .failureHandler(failureHandler)
                .toResultFuture();
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
    public Interceptor.Chain chosenInterceptorChain() {
        return chosenInterceptorChain;
    }

    @Override
    public Stage.Chain replyStageChain() {
        return replyStageChain;
    }

    @Override
    public Interceptor.Chain replyInterceptorChain() {
        return replyInterceptorChain;
    }

    protected <T extends ReplyFuture> T doCall(Object[] args, Interaction.Mode<T> mode) {
        Request request = protocol().createRequest(this, args);
        CallContext<Request, Caller<?>> context = new CallContext<>(module, request, this, mode, args);
        MetricsSupport.recordStartTime(context);
        Interaction.Result result = callStageChain().proceed(context);
        return result.excepted();
    }

    /**
     * Assembles a complete {@link Caller} before registering it.
     */
    public abstract static class Builder<T extends Caller<?>, SELF extends Builder<T, SELF>>
            extends AbstractPeer.Builder<T, SELF> {

        protected Locator locator;

        protected CallerGroup<?> group;

        protected ClientConfig clientConfig;

        protected Unary.FailureHandler failureHandler;

        protected Interceptor.Chain chosenInterceptorChain;

        protected Builder(TypeCapture<?> returnType, String protocol) {
            super(protocol);
            this.replyType = AssertUtil.notNull(returnType, "returnType");
        }

        @Override
        protected Class<? extends Peer> peerType() {
            return Caller.class;
        }

        @Override
        protected PeerDescriptor.Kind kind() {
            return PeerDescriptor.Kind.CALLER;
        }

        @Override
        protected void prepare() {
            this.locator = ensureLocator(descriptor);
            this.clientConfig = ensureClientConfig(descriptor);
            this.failureHandler = module.platform().preferredExtension(
                    Unary.FailureHandler.class,
                    descriptor.options().option(FAILURE_HANDLER)
            );
        }

        @Override
        protected void resolveComponents() {
            super.resolveComponents();
            if (chosenInterceptorChain == null) {
                InterceptorChainResolver resolver = module.preferredExtension(
                        InterceptorChainResolver.class,
                        descriptor.options().option(INTERCEPTOR_CHAIN_RESOLVER)
                );
                chosenInterceptorChain = resolver.resolveChosenChain(descriptor, module);
            }
        }

        @Override
        protected void checkState() {
            super.checkState();
            AssertUtil.notNull(locator, "locator");
            AssertUtil.notNull(clientConfig, "clientConfig");
            AssertUtil.notNull(failureHandler, "failureHandler");
            AssertUtil.notNull(chosenInterceptorChain, "chosenInterceptorChain");
        }

        public ClientConfig clientConfig() {
            return clientConfig;
        }

        public SELF clientConfig(ClientConfig clientConfig) {
            this.clientConfig = clientConfig;
            return self();
        }

        public Locator locator() {
            return locator;
        }

        public SELF locator(Locator locator) {
            this.locator = locator;
            return self();
        }

        public Interceptor.Chain chosenInterceptorChain() {
            return chosenInterceptorChain;
        }

        public SELF chosenInterceptorChain(Interceptor.Chain chain) {
            this.chosenInterceptorChain = chain;
            return self();
        }

        public SELF endpoint(String target) {
            addOption(ENDPOINT, target);
            return self();
        }

        public SELF registryConfigs(String... registryConfigs) {
            if (CollectionUtil.isNotEmpty(registryConfigs)) {
                addOption(REGISTRY, registryConfigs);
            }
            return self();
        }

        public SELF timeout(int timeout) {
            addOption(TIMEOUT, timeout);
            return self();
        }

        public SELF serviceDiscoveryTimeout(int timeout) {
            addOption(SERVICE_DISCOVERY_TIMEOUT, timeout);
            return self();
        }

        public SELF loadBalancer(String loadBalancer) {
            addOption(LOAD_BALANCER, loadBalancer);
            return self();
        }

        public SELF failureHandler(String failureHandler) {
            addOption(FAILURE_HANDLER, failureHandler);
            return self();
        }

        public SELF group(CallerGroup<?> group) {
            this.group = group;
            return self();
        }

        @Override
        protected CallerGroup<?> group() {
            return group;
        }

        private Locator ensureLocator(PeerDescriptor descriptor) {
            if (locator != null) {
                return locator;
            }
            String locatorName = descriptor.options().option(LOCATOR);
            LocatorResolver resolver = module.platform().preferredExtension(LocatorResolver.class, locatorName);
            return resolver.resolve(descriptor, module.platform());
        }

        private ClientConfig ensureClientConfig(PeerDescriptor descriptor) {
            if (clientConfig != null) {
                return clientConfig;
            }
            String clientConfigName = descriptor.options().option(CLIENT);
            if (StringUtil.isBlank(clientConfigName)) {
                return DefaultClientConfig.cached(
                        descriptor.protocol().name(),
                        descriptor.protocol().stack()
                );
            }
            ClientConfig configured = module.platform().namedComponent(ClientConfig.class, clientConfigName);
            return configured == null
                    ? DefaultClientConfig.cached(
                            descriptor.protocol().name(),
                            descriptor.protocol().stack()
                    )
                    : configured;
        }
    }
}
