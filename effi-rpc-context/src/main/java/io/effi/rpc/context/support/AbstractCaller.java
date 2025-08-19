package io.effi.rpc.context.support;

import io.effi.rpc.async.Future;
import io.effi.rpc.component.transport.ClientConfig;
import io.effi.rpc.component.transport.DefaultClientConfig;
import io.effi.rpc.config.ConfigNames;
import io.effi.rpc.config.HierarchicalConfig;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.ConfigurableCaller;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.Interceptor;
import io.effi.rpc.context.Locator;
import io.effi.rpc.context.LocatorFactory;
import io.effi.rpc.context.RemoteClient;
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

/**
 * Provides an abstract implementation of {@link Caller}.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class AbstractCaller<R> extends AbstractPeer<AbstractCaller.Builder> implements ConfigurableCaller<R> {

    protected Locator locator;

    protected UnaryReplyFuture.FailureHandler failureHandler;

    protected ClientConfig clientConfig;

    protected Interceptor.Chain chosenInterceptorChain;

    protected AbstractCaller(Builder builder) {
        super(builder);
    }

    @Override
    public Future<R> call(Object... args) throws EffiRpcException {
        return  doCall(args, UNARY_MODE)
                .withFailureHandler(failureHandler)
                .toResultFuture();
    }

    @Override
    public ConfigurableCaller<R> withClientConfig(ClientConfig clientConfig) {
        this.clientConfig = AssertUtil.notNull(clientConfig, "clientConfig");
        return this;
    }

    @Override
    public ConfigurableCaller<R> withLocator(Locator locator) {
        this.locator = AssertUtil.notNull(locator, "locator");
        return this;
    }

    @Override
    public ConfigurableCaller withChosenInterceptorChain(Interceptor.Chain chain) {
        this.chosenInterceptorChain = AssertUtil.notNull(chain, "chain");
        return this;
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

    @Override
    protected void initialize(Builder builder) {
        super.initialize(builder);
        this.replyType = builder.replyType;
        this.locator = ensureLocator(builder);
        this.clientConfig = ensureClientConfig(builder);
        this.failureHandler = platform().preferredExtension(
                UnaryReplyFuture.FailureHandler.class,
                getConfig(ConfigNames.FAILURE_HANDLER)
        );
    }

    @Override
    protected void onInitialized(Builder builder) {
        super.onInitialized(builder);
        set(KeyConstant.LAST_CALL_INDEX, new AtomicInteger(-1));
        set(CallerMetrics.GENERIC_KEY, new CallerMetrics());
        module.registry().register(Caller.class, this);
    }

    private Locator ensureLocator(Builder builder) {
        Locator locator = builder.locator;
        if (locator != null) return locator;
        String target = getConfig(ConfigNames.TARGET);
        String locatorName = getConfig(ConfigNames.LOCATOR);
        LocatorFactory locatorFactory = platform().preferredExtension(LocatorFactory.class, locatorName);
        return locatorFactory.fetch(target, this);
    }

    private ClientConfig ensureClientConfig(Builder builder) {
        ClientConfig clientConfig = builder.clientConfig;
        if (clientConfig != null) return clientConfig;
        String clientConfigName = getConfig(ConfigNames.CLIENT_CONFIG);
        if (StringUtil.isBlank(clientConfigName)) {
            return DefaultClientConfig.fetch(protocol.name(), protocol.stack());
        } else {
            clientConfig = platform().namedComponent(ClientConfig.class, clientConfigName);
            if (clientConfig != null) return clientConfig;
        }
        return DefaultClientConfig.fetch(protocol.name(), protocol.stack());
    }

    protected <T extends ReplyFuture> T doCall(Object[] args, Interaction.Mode<T> mode) {
        Request request = protocol.createRequest(this, args);
        CallContext<Request, Caller<?>> context = new CallContext<>(module, request, this, mode, args);
        MetricsSupport.recordStartTime(context);
        Interaction.Result result = callStageChain().proceed(context);
        return result.excepted();
    }

    /**
     * Builds {@link Caller} instance and defines configuration.
     */
    public abstract static class Builder<T extends Caller<?>, SELF extends Builder<T, SELF>>
            extends AbstractPeer.Builder<T, SELF> {

        protected Locator locator;

        protected ClientConfig clientConfig;

        protected Interceptor.Chain chosenInterceptorChain;

        protected Builder(TypeCapture<?> returnType, String protocol, HierarchicalConfig config) {
            super(protocol, config);
            this.replyType = AssertUtil.notNull(returnType, "returnType");
        }

        public SELF registryConfigs(String... registryConfigs) {
            if (CollectionUtil.isNotEmpty(registryConfigs)) {
                config.set(ConfigNames.REGISTRY, registryConfigs);
            }
            return self();
        }

        public SELF target(String target) {
            config.set(ConfigNames.TARGET, target);
            return self();
        }

        public SELF container(RemoteClient<?> client) {
            this.container = client;
            return self();
        }

        public SELF locator(Locator locator) {
            this.locator = locator;
            return self();
        }

        public SELF clientConfig(ClientConfig clientConfig) {
            this.clientConfig = clientConfig;
            return self();
        }

        public SELF chosenInterceptorChain(Interceptor.Chain chain) {
            this.chosenInterceptorChain = chain;
            return self();
        }

        public SELF retries(int retries) {
            config.set(ConfigNames.RETRIES, retries);
            return self();
        }

        public SELF loadBalance(String loadBalance) {
            config.set(ConfigNames.LOAD_BALANCE, loadBalance);
            return self();
        }

        public SELF failureHandler(String failureHandler) {
            config.set(ConfigNames.FAILURE_HANDLER, failureHandler);
            return self();
        }

        public SELF timeout(int timeout) {
            config.set(ConfigNames.TIMEOUT, timeout);
            return self();
        }
    }

}
