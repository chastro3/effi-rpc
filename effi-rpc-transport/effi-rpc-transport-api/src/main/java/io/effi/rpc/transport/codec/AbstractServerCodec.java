package io.effi.rpc.transport.codec;

import io.effi.rpc.base.Callee;
import io.effi.rpc.base.Message;
import io.effi.rpc.base.context.CallContext;
import io.effi.rpc.base.event.EventDispatcher;
import io.effi.rpc.base.parameter.ParameterMapper;
import io.effi.rpc.base.parameter.ParameterParser;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.metrics.CalleeMetrics;
import io.effi.rpc.metrics.MetricsSupport;
import io.effi.rpc.metrics.constant.MetricsKey;
import io.effi.rpc.metrics.event.CalleeMetricsEvent;
import io.effi.rpc.transport.DefaultWrappedRequest;
import io.effi.rpc.transport.WrappedRequest;
import io.effi.rpc.transport.WrappedResponse;
import io.effi.rpc.transport.endpoint.Channel;
import io.effi.rpc.util.ReflectionUtil;

import java.lang.reflect.Parameter;

/**
 * Provides an abstract implementation of {@link ServerCodec}.
 */
public abstract class AbstractServerCodec<RESP extends Message.Response, REQ extends Message.Request> implements ServerCodec {

    @SuppressWarnings("unchecked")
    @Override
    public Message.Response encode(WrappedResponse<Callee> wrappedResponse) {
        RESP response = (RESP) wrappedResponse.response();
        if (!response.isInstance()) {
            return response;
        }
        var replyContext = wrappedResponse.context();
        var context = replyContext.callContext();
        MetricsSupport.recordSerializeStartTime(context);
        try {
            return encodeResponse(wrappedResponse, response);
        } catch (Exception e) {
            throw PredefinedErrorCode.ENCODE.fail(e, Message.Response.class, wrappedResponse.getClass());
        } finally {
            MetricsSupport.recordSerializeEndTime(context);
            Callee callee = replyContext.callSide();
            CalleeMetrics calleeMetrics = callee.get(CalleeMetrics.GENERIC_KEY);
            context.platform().lookup(EventDispatcher.class)
                    .publish(new CalleeMetricsEvent(calleeMetrics, context, replyContext.result().hasException()));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public WrappedRequest<Callee> decode(Channel channel, Message.Request request, Callee callee) {
        long startTime = System.nanoTime();
        try {
            ParameterMapper<ParameterParser<?>>[] parameterMappers = callee.parameterMappers();
            Object[] args = new Object[parameterMappers.length];
            for (int i = 0; i < parameterMappers.length; i++) {
                ParameterMapper<ParameterParser<?>> parameterMapper = parameterMappers[i];
                ParameterParser<?> parser = parameterMapper.value();
                if (parser != null) {
                    Parameter parameter = parameterMapper.parameter();
                    Object result = ((ParameterParser<Message.Request>) parser).parse(request, parameter, callee);
                    args[i] = ReflectionUtil.convertToParameterType(result, parameter);
                }
            }
            CallContext<Message.Request, Callee> context = new CallContext<>(callee.module(), request, callee, args);
            MetricsSupport.recordDeserializeEndTime(context);
            context.set(MetricsKey.DESERIALIZE_START_TIME, startTime);
            return new DefaultWrappedRequest<>(context, channel);
        } catch (Exception e) {
            throw PredefinedErrorCode.ENCODE.fail(e, DefaultWrappedRequest.class, request.getClass());
        }
    }

    protected abstract Message.Response encodeResponse(WrappedResponse<Callee> wrappedResponse, RESP response) throws Exception;

}
