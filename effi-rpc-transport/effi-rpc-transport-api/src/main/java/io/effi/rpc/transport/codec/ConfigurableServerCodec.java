package io.effi.rpc.transport.codec;

import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.ReplyContext;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.Response;
import io.effi.rpc.context.Servant;
import io.effi.rpc.context.invocation.Invocation;
import io.effi.rpc.context.metrics.ServantMetrics;
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

    /**
     * Sets the response encoder.
     *
     * @param encoder response encoder
     * @return this codec
     */
    public ConfigurableServerCodec<RESP, REQ> encoder(Encoder<RESP> encoder) {
        this.encoder = encoder;
        return this;
    }

    /**
     * Sets the request decoder.
     *
     * @param decoder request decoder
     * @param <C>     servant type
     * @return this codec
     */
    @SuppressWarnings("unchecked")
    public <C extends Servant> ConfigurableServerCodec<RESP, REQ> decoder(Decoder<REQ, C> decoder) {
        this.decoder = (Decoder<REQ, Servant>) decoder;
        return this;
    }

    /**
     * Sets the invocation resolver.
     *
     * @param invocationResolver invocation resolver
     * @return this codec
     */
    public ConfigurableServerCodec<RESP, REQ> invocationResolver(InvocationResolver invocationResolver) {
        this.invocationResolver = invocationResolver;
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public OutputMessage encode(ReplyContext<Response, Servant> context, Channel channel) {
        RESP response = (RESP) context.message();
        ServantMetrics metrics = ServantMetrics.of(context.peer());
        long start = System.nanoTime();
        try {
            return encoder.encode(response, channel);
        } catch (Exception e) {
            throw TransportErrorCodes.ENCODE.fail(e, OutputMessage.class, response.getClass());
        } finally {
            metrics.recordSerialization(System.nanoTime() - start);
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
        ServantMetrics metrics = ServantMetrics.of(servant);
        long startTime = System.nanoTime();
        try {
            REQ request = decoder.decode(inputMessage, servant);
            MethodBinder methodBinder = servant.methodBinder();
            Invocation invocation = invocationResolver.resolve(request, servant);
            Object[] args = methodBinder.resolve(invocation);
            CallContext<Request, Servant> context = new CallContext<>(servant.module(), request, servant, null, args);
            metrics.recordDeserialization(System.nanoTime() - startTime);
            return context;
        } catch (Exception e) {
            throw TransportErrorCodes.DECODE.fail(e, CallContext.class, inputMessage.getClass());
        }
    }
}
