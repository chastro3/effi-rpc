package io.effi.rpc.boot;

import io.effi.rpc.base.Callee;
import io.effi.rpc.base.Envelope;
import io.effi.rpc.base.RemoteService;
import io.effi.rpc.base.Result;
import io.effi.rpc.base.context.InvocationContext;
import io.effi.rpc.base.context.ReplyContext;
import io.effi.rpc.base.filter.FilterChain;
import io.effi.rpc.base.filter.InvokeFilter;
import io.effi.rpc.base.filter.ReplyFilter;
import io.effi.rpc.base.parameter.MethodMapper;
import io.effi.rpc.base.parameter.ParameterMapper;
import io.effi.rpc.base.parameter.ParameterParser;
import io.effi.rpc.config.DefaultConfigKeys;
import io.effi.rpc.config.NodeConfig;
import io.effi.rpc.config.URL;
import io.effi.rpc.config.URLType;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.boot.builder.CalleeBuilder;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.metrics.CalleeMetrics;
import io.effi.rpc.transport.TransportSupport;
import io.effi.rpc.util.DateUtil;
import io.effi.rpc.util.Ordered;
import io.effi.rpc.util.TypeToken;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Provides an abstract implementation of {@link Callee}.
 */
public abstract class AbstractCallee<T> extends AbstractInvoker<Object, CalleeBuilder<?, ?>> implements Callee<T> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractCallee.class);

    protected int methodIndex;

    protected MethodMapper<T> methodMapper;

    protected String desc;


    protected AbstractCallee(NodeConfig config, CalleeBuilder<?, ?> builder) {
        super(config, builder);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void initialize(NodeConfig config, CalleeBuilder<?, ?> builder) {
        super.initialize(config, builder);
        this.methodMapper = (MethodMapper<T>) builder.methodMapper();
        this.desc = config.get(DefaultConfigKeys.CALLEE_DESC);
        this.returnType = TypeToken.get(method().getGenericReturnType());
        this.methodIndex = remoteService().getCalleeIndex(this);
    }

    @Override
    protected void onInitialized(NodeConfig config, CalleeBuilder<?, ?> builder) {
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
    public RemoteService<T> remoteService() {
        return methodMapper.remoteService();
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

    public MethodMapper<T> methodMapper() {
        return methodMapper;
    }

    @Override
    public ReplyContext<Envelope.Response, Callee<?>> invokeWithContext(InvocationContext<Envelope.Request, Callee<?>> context) {
        URL url = context.envelope().url();
        AtomicReference<ReplyContext<Envelope.Response, Callee<?>>> replyContext = new AtomicReference<>();
        List<InvokeFilter<?, ?>> invokeFilters = Ordered.sort(this.invokeFilters);
        List<ReplyFilter<?, ?>> replyFilters = Ordered.sort(this.replyFilters);
        var filterContext = context.executor(() -> {
            Object returnValue = null;
            try {
                if (URLType.CALLER.match(url)) {
                    long timeout = url.getLongParam(KeyConstant.TIMEOUT);
                    String timestamp = url.getParam(KeyConstant.TIMESTAMP);
                    LocalDateTime localDateTime = DateUtil.parse(timestamp);
                    long margin = Duration.between(localDateTime, LocalDateTime.now()).toMillis();
                    if (margin < timeout) {
                        returnValue = invoke(context.args());
                        long invokeAfterMargin = Duration.between(localDateTime, LocalDateTime.now()).toMillis();
                        if (invokeAfterMargin > timeout) {
                            returnValue = null;
                        }
                    }
                } else {
                    returnValue = invoke(context.args());
                }
            } catch (EffiRpcException e) {
                returnValue = e;
            }
            Result result = Result.create(url, returnValue);
            Envelope.Response response;
            if (url.protocol().equals(protocol())) {
                response = protocol.createResponse(this, result);
            } else {
                response = TransportSupport.getProtocol(url.protocol()).createResponse(this, result);
            }
            replyContext.set(new ReplyContext<>(context, response, result));
            var replyFilterContext = replyContext.get().executor(() -> result);
            return FilterChain.execute(replyFilterContext, replyFilters);
        });
        FilterChain.execute(filterContext, invokeFilters);
        return replyContext.get();
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
}
