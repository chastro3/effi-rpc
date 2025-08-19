package io.effi.rpc.transport.codec;

import io.effi.rpc.component.event.EventDispatcher;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Callee;
import io.effi.rpc.context.ReplyContext;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.Response;
import io.effi.rpc.context.metrics.CalleeMetrics;
import io.effi.rpc.context.metrics.MetricsSupport;
import io.effi.rpc.context.metrics.constant.MetricsKey;
import io.effi.rpc.context.metrics.event.CalleeMetricsEvent;
import io.effi.rpc.context.parameter.ParameterMapper;
import io.effi.rpc.context.parameter.ParameterParser;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.transport.endpoint.Channel;
import io.effi.rpc.transport.message.InputMessage;
import io.effi.rpc.transport.message.OutputMessage;
import io.effi.rpc.util.ReflectionUtil;

import java.lang.reflect.Parameter;

/**
 * Provides the default implementation of {@link ServerExchangeContextCodec}.
 */
public class DefaultServerExchangeContextCodec<RESP extends Response, REQ extends Request>
        implements ServerExchangeContextCodec {

    private Encoder<RESP> encoder;

    private Decoder<REQ, Callee> decoder;

    public DefaultServerExchangeContextCodec<RESP, REQ> withEncoder(Encoder<RESP> encoder) {
        this.encoder = encoder;
        return this;
    }

    @SuppressWarnings("unchecked")
    public <C extends Callee> DefaultServerExchangeContextCodec<RESP, REQ> withDecoder(Decoder<REQ, C> decoder) {
        this.decoder = (Decoder<REQ, Callee>) decoder;
        return this;
    }


    @SuppressWarnings("unchecked")
    @Override
    public OutputMessage encode(ReplyContext<Response, Callee> context, Channel channel) {
        RESP response = (RESP) context.message();
        var callContext = context.callContext();
        MetricsSupport.recordSerializeStartTime(callContext);
        try {
            return encoder.encode(response, channel);
        } catch (Exception e) {
            throw PredefinedErrorCode.ENCODE.fail(e, OutputMessage.class, response.getClass());
        } finally {
            MetricsSupport.recordSerializeEndTime(callContext);
            Callee callee = context.peer();
            CalleeMetrics calleeMetrics = callee.get(CalleeMetrics.GENERIC_KEY);
            context.platform().singleComponent(EventDispatcher.class)
                    .publish(new CalleeMetricsEvent(calleeMetrics, callContext, context.result().succeeded()));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public CallContext<Request, Callee> decode(InputMessage inputMessage, Callee callee) {
        long startTime = System.nanoTime();
        try {
            REQ request = decoder.decode(inputMessage, callee);
            ParameterMapper<ParameterParser<?>>[] parameterMappers = callee.parameterMappers();
            Object[] args = new Object[parameterMappers.length];
            for (int i = 0; i < parameterMappers.length; i++) {
                ParameterMapper<ParameterParser<?>> parameterMapper = parameterMappers[i];
                ParameterParser<?> parser = parameterMapper.value();
                if (parser != null) {
                    Parameter parameter = parameterMapper.parameter();
                    Object result = ((ParameterParser<Request>) parser).parse(request, parameter, callee);
                    args[i] = ReflectionUtil.convertToParameterType(result, parameter);
                }
            }
            CallContext<Request, Callee> context = new CallContext<>(callee.module(), request, callee, null, args);
            MetricsSupport.recordDeserializeEndTime(context);
            context.set(MetricsKey.DESERIALIZE_START_TIME, startTime);
            return context;
        } catch (Exception e) {
            throw PredefinedErrorCode.ENCODE.fail(e, CallContext.class, inputMessage.getClass());
        }
    }
}
