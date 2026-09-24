package io.effi.rpc.context.support;

import io.effi.rpc.context.InteractionErrorCodes;
import io.effi.rpc.context.Peer;
import io.effi.rpc.context.PeerDescriptor;
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

import static io.effi.rpc.context.options.ServantOptions.LABEL;

/**
 * Provides an immutable implementation of {@link Servant}.
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractServant extends AbstractPeer<AbstractServant.Builder> implements Servant {

    private static final Logger logger = LoggerFactory.getLogger(AbstractServant.class);

    protected final int methodIndex;

    protected final ServantMethod<?> servantMethod;

    protected final String label;

    protected AbstractServant(Builder builder) {
        super(builder);
        this.servantMethod = builder.servantMethod;
        this.label = builder.label();
        this.methodIndex = group().indexOf(this);
        set(CalleeMetrics.GENERIC_KEY, new CalleeMetrics());
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
     * Assembles a complete {@link Servant} before registering it.
     */
    public abstract static class Builder<T extends Servant, SELF extends Builder<T, SELF>>
            extends AbstractPeer.Builder<T, SELF> {

        protected ServantMethod<?> servantMethod;

        protected ServantGroup<?> group;

        protected Builder(ServantMethod<?> servantMethod, String protocol) {
            super(protocol);
            this.servantMethod = AssertUtil.notNull(servantMethod, "servantMethod");
            this.group = servantMethod.group();
            this.replyType = TypeCapture.of(servantMethod.method().getGenericReturnType());
        }

        @Override
        protected Class<? extends Peer> peerType() {
            return Servant.class;
        }

        @Override
        protected PeerDescriptor.Kind kind() {
            return PeerDescriptor.Kind.SERVANT;
        }

        @Override
        protected ServantGroup<?> group() {
            return group;
        }

        public String label() {
            return option(LABEL);
        }

        public SELF label(String label) {
            addOption(LABEL, label);
            return self();
        }
    }
}
