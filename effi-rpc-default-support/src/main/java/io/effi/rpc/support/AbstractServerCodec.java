package io.effi.rpc.support;

import io.effi.rpc.common.exception.EffiRpcException;
import io.effi.rpc.common.exception.PredefinedErrorCode;
import io.effi.rpc.common.url.URL;
import io.effi.rpc.common.util.ReflectionUtil;
import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.context.InvocationContext;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.contract.parameter.ParameterMapper;
import io.effi.rpc.contract.parameter.ParameterParser;
import io.effi.rpc.metrics.CalleeMetrics;
import io.effi.rpc.metrics.MetricsSupport;
import io.effi.rpc.metrics.constant.MetricsKey;
import io.effi.rpc.metrics.event.CalleeMetricsEvent;
import io.effi.rpc.transport.NettyChannel;
import io.effi.rpc.transport.RequestWrapper;
import io.effi.rpc.transport.ResponseWrapper;
import io.effi.rpc.transport.codec.ServerCodec;

import java.lang.reflect.Parameter;

/**
 * Abstract implementation of {@link ServerCodec}.
 *
 * @param <RESP> the type of response
 * @param <REQ>  the type of request
 */
public abstract class AbstractServerCodec<RESP extends Envelope.Response, REQ extends Envelope.Request> implements ServerCodec {

    @SuppressWarnings("unchecked")
    @Override
    public Envelope.Response encode(NettyChannel channel, ResponseWrapper<Callee<?>> responseWrapper) {
        RESP response = (RESP) responseWrapper.response();
        if (!response.isInstance()) {
            return response;
        }
        var context = responseWrapper.context().invocationContext();
        MetricsSupport.recordSerializeStartTime(context);
        try {
            return encodeResponse(responseWrapper, response);
        } catch (Exception e) {
            throw EffiRpcException.wrap(PredefinedErrorCode.ENCODE, e, Envelope.Response.class,
                    responseWrapper.response().getClass());
        } finally {
            MetricsSupport.recordSerializeEndTime(context);
            Callee<?> callee = responseWrapper.context().invoker();
            CalleeMetrics calleeMetrics = callee.get(CalleeMetrics.GENERIC_KEY);
            context.module()
                    .application()
                    .publishEvent(new CalleeMetricsEvent(calleeMetrics, context, responseWrapper.result().hasException()));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public RequestWrapper<Callee<?>> decode(NettyChannel channel, Envelope.Request request) {
        long startTime = System.nanoTime();
        try {
            URL requestURL = request.url();
            EffiRpcModule module = channel.module();
            Callee<?> callee = findCallee(channel, requestURL, module);
            ParameterMapper<ParameterParser<?>>[] parameterMappers = callee.parameterMappers();
            Object[] args = new Object[parameterMappers.length];
            for (int i = 0; i < parameterMappers.length; i++) {
                ParameterMapper<ParameterParser<?>> parameterMapper = parameterMappers[i];
                ParameterParser<?> parser = parameterMapper.value();
                if (parser != null) {
                    Parameter parameter = parameterMapper.parameter();
                    Object result = ((ParameterParser<Envelope.Request>) parser).parse(request, parameter, callee);
                    args[i] = ReflectionUtil.convertToParameterType(result, parameter);
                }
            }
            InvocationContext<Envelope.Request, Callee<?>> context = new InvocationContext<>(module, request, callee, args);
            MetricsSupport.recordDeserializeEndTime(context);
            context.set(MetricsKey.DESERIALIZE_START_TIME, startTime);
            return RequestWrapper.parse(context);
        } catch (Exception e) {
            throw EffiRpcException.wrap(PredefinedErrorCode.ENCODE, e, request.getClass(), RequestWrapper.class);
        }
    }

    protected Callee<?> findCallee(NettyChannel channel, URL url, EffiRpcModule module) {
        Callee<?> callee = module.serverExporterManager().acquireCallee(url);
        if (callee == null) {
            // todo send to client
            throw EffiRpcException.wrap(PredefinedErrorCode.NOT_FOUND_CALLEE, url.authority());
        }
        return callee;
    }

    protected abstract Envelope.Response encodeResponse(ResponseWrapper<Callee<?>> responseWrapper, RESP response) throws Exception;

}
