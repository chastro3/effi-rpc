package io.effi.rpc.context.support;

import io.effi.rpc.context.ConfigurableServant;
import io.effi.rpc.context.InteractionErrorCodes;
import io.effi.rpc.context.Servant;
import io.effi.rpc.context.ServantGroup;
import io.effi.rpc.context.metrics.CalleeMetrics;
import io.effi.rpc.context.parameter.ParameterBinding;
import io.effi.rpc.context.parameter.ServantMethod;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.TypeCapture;

import java.lang.reflect.Method;

/**
 * Provides an abstract implementation of {@link Servant}.
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractServant extends AbstractPeer<AbstractServant.Builder> implements ConfigurableServant {

    private static final Logger logger = LoggerFactory.getLogger(AbstractServant.class);

    protected int methodIndex;

    protected ServantMethod<?> servantMethod;

    protected String label;

    protected AbstractServant(Builder builder) {
        super(builder);
    }

    @Override
    protected void initialize(Builder builder) {
        super.initialize(builder);
        this.servantMethod = builder.servantMethod;
        this.label = option(LABEL);
        this.replyType = TypeCapture.of(method().getGenericReturnType());
        this.methodIndex = group().indexOf(this);
    }

    @Override
    protected void onInitialized(Builder builder) {
        super.onInitialized(builder);
        set(CalleeMetrics.GENERIC_KEY, new CalleeMetrics());
        module.registry().register(Servant.class, this);
        group().register(this);
    }

    @Override
    public ConfigurableServant label(String label) {
        this.label = AssertUtil.notBlank(label, "label");
        return this;
    }

    @Override
    public int methodIndex() {
        return methodIndex;
    }

    @Override
    public Method method() {
        return servantMethod.method();
    }

    @Override
    public ParameterBinding[] parameterBindings() {
        return servantMethod.bindings();
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public ServantGroup<?> group() {
        return servantMethod.group();
    }

    public ServantMethod<?> methodMapper() {
        return servantMethod;
    }

    @Override
    public Object invoke(Object... args) throws EffiRpcException {
        try {
            return group().invoke(this, args);
        } catch (Exception e) {
            EffiRpcException exception = InteractionErrorCodes.SERVANT_INVOCATION_FAILED.fail(e, toString());
            logger.error(exception.getMessage(), e);
            throw exception;
        }
    }

    /**
     * Builds {@link Servant} instance and defines configuration.
     */
    public abstract static class Builder<T extends Servant, SELF extends Builder<T, SELF>> extends AbstractPeer.Builder<T, SELF> {

        protected ServantMethod<?> servantMethod;

        protected Builder(ServantMethod<?> servantMethod, String protocol) {
            super(protocol);
            this.servantMethod = AssertUtil.notNull(servantMethod, "servantMethod");
            this.group = servantMethod.group();
        }

        public SELF label(String desc) {
            addOption(Servant.LABEL, desc);
            return self();
        }
    }
}
