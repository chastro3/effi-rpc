package io.effi.rpc.engine;

import io.effi.rpc.common.config.*;
import io.effi.rpc.common.constant.Constant;
import io.effi.rpc.common.constant.KeyConstant;
import io.effi.rpc.common.exception.EffiRpcException;
import io.effi.rpc.common.exception.PredefinedErrorCode;
import io.effi.rpc.common.util.CollectionUtil;
import io.effi.rpc.common.util.DateUtil;
import io.effi.rpc.common.util.Ordered;
import io.effi.rpc.common.util.TypeToken;
import io.effi.rpc.contract.*;
import io.effi.rpc.contract.context.InvocationContext;
import io.effi.rpc.contract.context.ReplyContext;
import io.effi.rpc.contract.filter.Filter;
import io.effi.rpc.contract.filter.FilterChain;
import io.effi.rpc.contract.filter.InvokeFilter;
import io.effi.rpc.contract.filter.ReplyFilter;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.contract.module.ServerExporter;
import io.effi.rpc.contract.parameter.MethodMapper;
import io.effi.rpc.contract.parameter.ParameterMapper;
import io.effi.rpc.contract.parameter.ParameterParser;
import io.effi.rpc.engine.builder.CalleeBuilder;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.metrics.CalleeMetrics;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Abstract implementation of {@link Callee}.
 *
 * @param <T> the type of service
 */
public abstract class AbstractCallee<T> extends AbstractInvoker<Object> implements Callee<T> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractCallee.class);

    protected int methodIndex;

    protected Map<EffiRpcModule, CalleeModularConfig> modularConfigMap = new ConcurrentHashMap<>();

    protected MethodMapper<T> methodMapper;

    protected String desc;

    @SuppressWarnings("unchecked")
    protected AbstractCallee(LinkedConfig config, CalleeBuilder<?, ?> builder) {
        super(config, builder);
        this.methodMapper = (MethodMapper<T>) builder.methodMapper();
        this.desc = config.get(DefaultConfigKeys.CALLEE_DESC);
        this.returnType = new TypeToken<>(method().getGenericReturnType()) {};
        this.methodIndex = remoteService().getCalleeIndex(this);
        export(builder.modules().toArray(EffiRpcModule[]::new));
        addFilter(builder.filters().toArray(Filter[]::new));
        set(CalleeMetrics.GENERIC_KEY, new CalleeMetrics());
        remoteService().addCallee(this);
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
    public void export(EffiRpcModule... modules) {
        if (CollectionUtil.isNotEmpty(modules)) {
            List<String> excludedPorts = getCascaded(DefaultConfigKeys.EXCLUDED_PORT);
            for (EffiRpcModule module : modules) {
                modularConfigMap.put(module, new CalleeModularConfig(module, this));
                for (ServerExporter serverExporter : module.serverExporterManager().components()) {
                    URL url = serverExporter.url();
                    if (protocol().equals(url.protocol()) && !excludedPorts.contains(String.valueOf(url.port()))) {
                        serverExporter.callee(this);
                    }
                }
            }
        }
    }

    @Override
    public Collection<EffiRpcModule> exportedModules() {
        return Collections.unmodifiableCollection(modularConfigMap.keySet());
    }

    @Override
    public void addFilter(Filter<?, ?, ?>... filters) {
        for (CalleeModularConfig modularConfig : modularConfigMap.values()) {
            modularConfig.addFilter(filters);
        }
    }

    @Override
    public ReplyContext<Envelope.Response, Callee<?>> invokeWithContext(InvocationContext<Envelope.Request, Callee<?>> context) {
        URL url = context.source().url();
        AtomicReference<ReplyContext<Envelope.Response, Callee<?>>> replyContext = new AtomicReference<>();
        CalleeModularConfig modularConfig = modularConfigMap.get(context.module());
        List<InvokeFilter<?, ?>> invokeFilters = Ordered.sort(modularConfig.invokeFilters());
        List<ReplyFilter<?, ?>> replyFilters = Ordered.sort(modularConfig.replyFilters());
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
            Result result = new Result(url, returnValue);
            Envelope.Response response = protocol.createResponse(this, result);
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
            logger.error(exception.getMessage());
            throw exception;
        }
    }

    @Override
    public ThreadPool threadPoolOf(EffiRpcModule module) {
        return getThreadPool(module, Constant.DEFAULT_SERVER_HYBRID_THREAD_POOL);
    }
}
