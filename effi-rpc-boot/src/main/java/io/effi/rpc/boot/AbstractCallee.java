package io.effi.rpc.boot;

import io.effi.rpc.base.Callee;
import io.effi.rpc.base.Message;
import io.effi.rpc.base.RemoteService;
import io.effi.rpc.base.context.CallContext;
import io.effi.rpc.base.context.ReplyContext;
import io.effi.rpc.base.parameter.MethodMapper;
import io.effi.rpc.base.parameter.ParameterMapper;
import io.effi.rpc.base.parameter.ParameterParser;
import io.effi.rpc.boot.builder.CalleeBuilder;
import io.effi.rpc.boot.stage.CallInterceptStage;
import io.effi.rpc.boot.stage.InvokeCalleeStage;
import io.effi.rpc.config.DefaultConfigNames;
import io.effi.rpc.config.NodeConfig;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.metrics.CalleeMetrics;
import io.effi.rpc.util.TypeToken;

import java.lang.reflect.Method;

/**
 * Provides an abstract implementation of {@link Callee}.
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractCallee extends AbstractCallSide<CalleeBuilder> implements Callee {

    private static final Logger logger = LoggerFactory.getLogger(AbstractCallee.class);

    private static final String[] DEFAULT_CALL_STAGE_CHAIN = new String[]{
            CallInterceptStage.NAME, InvokeCalleeStage.NAME
    };

    protected int methodIndex;
    protected MethodMapper<?> methodMapper;

    protected String desc;

    protected AbstractCallee(NodeConfig config, CalleeBuilder builder) {
        super(config, builder);
    }

    @Override
    protected void initialize(NodeConfig config, CalleeBuilder builder) {
        super.initialize(config, builder);
        this.methodMapper = builder.methodMapper();
        this.desc = config.get(DefaultConfigNames.CALLEE_DESC);
        this.returnType = TypeToken.get(method().getGenericReturnType());
        this.methodIndex = remoteService().getCalleeIndex(this);
    }

    @Override
    protected void onInitialized(NodeConfig config, CalleeBuilder builder) {
        super.onInitialized(config, builder);
        set(CalleeMetrics.GENERIC_KEY, new CalleeMetrics());
        remoteService().addCallee(this);
        module.register(Callee.class, this);
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

    @Override
    public ReplyContext<Message.Response, Callee> invokeWithContext(CallContext<Message.Request, Callee> context) {
        return null;
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

    @Override
    protected String[] defaultCallStageChain() {
        return DEFAULT_CALL_STAGE_CHAIN;
    }
}
