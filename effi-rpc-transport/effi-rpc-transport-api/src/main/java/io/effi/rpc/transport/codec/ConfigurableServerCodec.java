package io.effi.rpc.transport.codec;

import io.effi.rpc.component.event.EventDispatcher;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.ReplyContext;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.Response;
import io.effi.rpc.context.Servant;
import io.effi.rpc.context.invocation.Invocation;
import io.effi.rpc.context.metrics.CalleeMetrics;
import io.effi.rpc.context.metrics.MetricsSupport;
import io.effi.rpc.context.metrics.constant.MetricsKey;
import io.effi.rpc.context.metrics.event.CalleeMetricsEvent;
import io.effi.rpc.context.parameter.MethodBinder;
import io.effi.rpc.transport.TransportErrorCodes;
import io.effi.rpc.transport.endpoint.Channel;
import io.effi.rpc.transport.message.InputMessage;
import io.effi.rpc.transport.message.OutputMessage;

/**
 * Provides the default implementation of {@link ServerExchangeContextCodec}.
 */
public class ConfigurableServerCodec<RESP extends Response, REQ extends Request>
        implements ServerExchangeContextCodec {

    private Encoder<RESP> encoder;

    private Decoder<REQ, Servant> decoder;

    private InvocationResolver invocationResolver;

    public ConfigurableServerCodec<RESP, REQ> encoder(Encoder<RESP> encoder) {
        this.encoder = encoder;
        return this;
    }

    @SuppressWarnings("unchecked")
    public <C extends Servant> ConfigurableServerCodec<RESP, REQ> decoder(Decoder<REQ, C> decoder) {
        this.decoder = (Decoder<REQ, Servant>) decoder;
        return this;
    }

    public ConfigurableServerCodec<RESP, REQ> invocationResolver(InvocationResolver invocationResolver) {
        this.invocationResolver = invocationResolver;
        return this;
    }


    @SuppressWarnings("unchecked")
    @Override
    public OutputMessage encode(ReplyContext<Response, Servant> context, Channel channel) {
        RESP response = (RESP) context.message();
        var callContext = context.callContext();
        MetricsSupport.recordSerializeStartTime(callContext);
        try {
            return encoder.encode(response, channel);
        } catch (Exception e) {
            throw TransportErrorCodes.ENCODE.fail(e, OutputMessage.class, response.getClass());
        } finally {
            MetricsSupport.recordSerializeEndTime(callContext);
            Servant servant = context.peer();
            CalleeMetrics calleeMetrics = servant.get(CalleeMetrics.GENERIC_KEY);
            context.platform().singleComponent(EventDispatcher.class)
                    .publish(new CalleeMetricsEvent(calleeMetrics, callContext, context.result().succeeded()));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public OutputMessage encode(Response response, Channel channel) {
        try {
            return encoder.encode((RESP) response, channel);
        } catch (Exception e) {
            throw TransportErrorCodes.ENCODE.fail(e, OutputMessage.class, response.getClass());
        }
    }

    @Override
    public CallContext<Request, Servant> decode(InputMessage inputMessage, Servant servant) {
        long startTime = System.nanoTime();
        try {
            REQ request = decoder.decode(inputMessage, servant);
            MethodBinder methodBinder = servant.methodBinder();
            Invocation invocation = invocationResolver.resolve(request, servant);
            Object[] args = methodBinder.resolve(invocation);
            CallContext<Request, Servant> context = new CallContext<>(servant.module(), request, servant, null, args);
            MetricsSupport.recordDeserializeEndTime(context);
            context.set(MetricsKey.DESERIALIZE_START_TIME, startTime);
            return context;
        } catch (Exception e) {
            throw TransportErrorCodes.DECODE.fail(e, CallContext.class, inputMessage.getClass());
        }
    }
}
