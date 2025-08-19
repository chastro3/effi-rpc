package io.effi.rpc.transport.codec;

import io.effi.rpc.config.SmartURL;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.ReplyContext;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.Response;
import io.effi.rpc.context.metrics.MetricsSupport;
import io.effi.rpc.context.support.ReplyFuture;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.transport.endpoint.Channel;
import io.effi.rpc.transport.message.InputMessage;
import io.effi.rpc.transport.message.OutputMessage;

/**
 * Provides the default implementation of {@link ClientExchangeContextCodec}.
 */
public class DefaultClientExchangeContextCodec<REQ extends Request, RESP extends Response> implements ClientExchangeContextCodec {

    private Encoder<REQ> encoder;

    private Decoder<RESP, Caller<?>> decoder;

    private ResultExtractor<RESP> resultExtractor;

    public DefaultClientExchangeContextCodec<REQ, RESP> withEncoder(Encoder<REQ> encoder) {
        this.encoder = encoder;
        return this;
    }

    @SuppressWarnings("unchecked")
    public <C extends Caller<?>> DefaultClientExchangeContextCodec<REQ, RESP> withDecoder(Decoder<RESP, C> decoder) {
        this.decoder = (Decoder<RESP, Caller<?>>) decoder;
        return this;
    }

    public DefaultClientExchangeContextCodec<REQ, RESP> withResultExtractor(ResultExtractor<RESP> resultExtractor) {
        this.resultExtractor = resultExtractor;
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public OutputMessage encode(CallContext<Request, Caller<?>> context, Channel channel) {
        REQ request = (REQ) context.message();
        MetricsSupport.recordSerializeStartTime(context);
        try {
            return encoder.encode(request, channel);
        } catch (Exception e) {
            throw PredefinedErrorCode.ENCODE.fail(e, OutputMessage.class, request.getClass());
        } finally {
            MetricsSupport.recordSerializeEndTime(context);
        }
    }

    @Override
    public ReplyContext<Response, Caller<?>> decode(InputMessage inputMessage, Caller<?> caller) {
        SmartURL url = inputMessage.url();
        ReplyFuture future = ReplyFuture.lookup(url);
        CallContext<Request, Caller<?>> context = future.context();
        try {
            MetricsSupport.recordDeserializeStartTime(context);
            RESP response = decoder.decode(inputMessage, caller);
            return new ReplyContext<>(context, response, resultExtractor.extract(response));
        } catch (Exception e) {
            throw PredefinedErrorCode.DECODE.fail(e, inputMessage.getClass(), Response.class);
        } finally {
            if (context != null) MetricsSupport.recordDeserializeEndTime(context);
        }
    }

    /**
     * Extracts a {@link Result} from a given response.
     */
    public interface ResultExtractor<RESP extends Response> {

        /**
         * Extracts the {@link Result} from the specified response.
         *
         * @param response the response
         * @return the extracted {@link Result}
         */
        Interaction.Result extract(RESP response);
    }


}
