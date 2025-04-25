package io.effi.rpc.transport.codec;

import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.util.ReflectionUtil;
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
import io.effi.rpc.transport.DefaultRepackagedRequest;
import io.effi.rpc.transport.RepackagedRequest;
import io.effi.rpc.transport.RepackagedResponse;
import io.effi.rpc.transport.endpoint.Channel;

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
    public Envelope.Response encode(RepackagedResponse<Callee<?>> repackagedResponse) {
        RESP response = (RESP) repackagedResponse.response();
        if (!response.isInstance()) {
            return response;
        }
        var replyContext = repackagedResponse.context();
        var context = replyContext.invocationContext();
        MetricsSupport.recordSerializeStartTime(context);
        try {
            return encodeResponse(repackagedResponse, response);
        } catch (Exception e) {
            throw PredefinedErrorCode.ENCODE.fail(e, Envelope.Response.class, repackagedResponse.getClass());
        } finally {
            MetricsSupport.recordSerializeEndTime(context);
            Callee<?> callee = replyContext.invoker();
            CalleeMetrics calleeMetrics = callee.get(CalleeMetrics.GENERIC_KEY);
            context.module()
                    .application()
                    .publishEvent(new CalleeMetricsEvent(calleeMetrics, context, replyContext.result().hasException()));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public RepackagedRequest<Callee<?>> decode(Channel channel, Envelope.Request request, Callee<?> callee) {
        long startTime = System.nanoTime();
        try {
            EffiRpcModule module = channel.module();
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
            return new DefaultRepackagedRequest<>(context, channel);
        } catch (Exception e) {
            throw PredefinedErrorCode.ENCODE.fail(e, DefaultRepackagedRequest.class, request.getClass());
        }
    }

    protected abstract Envelope.Response encodeResponse(RepackagedResponse<Callee<?>> repackagedResponse, RESP response) throws Exception;

}
