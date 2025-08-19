package io.effi.rpc.context.support;

import io.effi.rpc.config.ConfigNames;
import io.effi.rpc.config.HierarchicalConfig;
import io.effi.rpc.context.Callee;
import io.effi.rpc.context.ConfigurableCallee;
import io.effi.rpc.context.RemoteService;
import io.effi.rpc.context.metrics.CalleeMetrics;
import io.effi.rpc.context.parameter.MethodMapper;
import io.effi.rpc.context.parameter.ParameterMapper;
import io.effi.rpc.context.parameter.ParameterParser;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.TypeCapture;

import java.lang.reflect.Method;

/**
 * Provides an abstract implementation of {@link Callee}.
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractCallee extends AbstractPeer<AbstractCallee.Builder> implements ConfigurableCallee {

    private static final Logger logger = LoggerFactory.getLogger(AbstractCallee.class);

    protected int methodIndex;

    protected MethodMapper<?> methodMapper;

    protected String desc;

    protected AbstractCallee(Builder builder) {
        super(builder);
    }

    @Override
    protected void initialize(Builder builder) {
        super.initialize(builder);
        this.methodMapper = builder.methodMapper;
        this.desc = config.get(ConfigNames.CALLEE_DESC);
        this.replyType = TypeCapture.of(method().getGenericReturnType());
        this.methodIndex = remoteService().getCalleeIndex(this);
    }

    @Override
    protected void onInitialized(Builder builder) {
        super.onInitialized(builder);
        set(CalleeMetrics.GENERIC_KEY, new CalleeMetrics());
        module.registry().register(Callee.class, this);
        remoteService().addCallee(this);
    }

    @Override
    public ConfigurableCallee withDesc(String desc) {
        this.desc = AssertUtil.notBlank(desc, "desc");
        return this;
    }

    @Override
    public int methodIndex() {
        return methodIndex;
    }

    @Override
    public Method method() {
        return methodMapper.method();
    }

    @Override
    public ParameterMapper<ParameterParser<?>>[] parameterMappers() {
        return methodMapper.parameterMappers();
    }

    @Override
    public String desc() {
        return desc;
    }

    @Override
    public RemoteService<?> remoteService() {
        return methodMapper.remoteService();
    }

    public MethodMapper<?> methodMapper() {
        return methodMapper;
    }

    @Override
    public Object invoke(Object... args) throws EffiRpcException {
        try {
            return remoteService().invokeCallee(this, args);
        } catch (Exception e) {
            EffiRpcException exception = PredefinedErrorCode.INVOKE_SERVICE.fail(e, toString());
            logger.error(exception.getMessage(), e);
            throw exception;
        }
    }

    /**
     * Builds {@link Callee} instance and defines configuration.
     */
    public abstract static class Builder<T extends Callee, SELF extends Builder<T, SELF>>
            extends AbstractPeer.Builder<T, SELF> {

        protected MethodMapper<?> methodMapper;

        protected Builder(MethodMapper<?> methodMapper, String protocol, HierarchicalConfig config) {
            super(protocol, config);
            this.methodMapper = AssertUtil.notNull(methodMapper, "methodMapper");
            this.container = methodMapper.remoteService();
        }

        public SELF desc(String desc) {
            config.set(ConfigNames.CALLEE_DESC, desc);
            return self();
        }
    }
}
