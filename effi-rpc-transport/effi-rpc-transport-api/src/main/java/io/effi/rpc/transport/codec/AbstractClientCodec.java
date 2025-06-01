package io.effi.rpc.transport.codec;

import io.effi.rpc.config.URL;
import io.effi.rpc.base.*;
import io.effi.rpc.base.context.InvocationContext;
import io.effi.rpc.base.context.ReplyContext;
import io.effi.rpc.base.parameter.ReplyParser;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.metrics.MetricsSupport;
import io.effi.rpc.transport.DefaultWrappedResponse;
import io.effi.rpc.transport.WrappedRequest;
import io.effi.rpc.transport.WrappedResponse;
import io.effi.rpc.transport.endpoint.Channel;

/**
 * Provides an abstract implementation of {@link ClientCodec}.
 */
public abstract class AbstractClientCodec<REQ extends Envelope.Request, RESP extends Envelope.Response> implements ClientCodec {

    protected ReplyParser<RESP> replyParser;

    @SuppressWarnings("unchecked")
    @Override
    public Envelope.Request encode(WrappedRequest<Caller<?>> wrappedRequest) {
        REQ request = (REQ) wrappedRequest.request();
        if (!request.isInstance()) {
            return request;
        }
        MetricsSupport.recordSerializeStartTime(wrappedRequest.context());
        try {
            return encodeRequest(wrappedRequest, request);
        } catch (Exception e) {
            throw PredefinedErrorCode.ENCODE.fail(e, Envelope.Request.class, wrappedRequest.getClass());
        } finally {
            MetricsSupport.recordSerializeEndTime(wrappedRequest.context());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public WrappedResponse<Caller<?>> decode(Channel channel, Envelope.Response response, ReplyFuture future) {
        InvocationContext<Envelope.Request, Caller<?>> context = future.context();
        try {
            URL requestUrl = response.url();
            MetricsSupport.recordDeserializeStartTime(context);
            Result result = null;
            if (!response.isInstance())
                result = replyParser.resolve((RESP) response, context.invoker());
            if (result == null) result = ResultType.resolve(requestUrl, null);
            var replyContext = new ReplyContext<>(context, response, result);
            return new DefaultWrappedResponse<>(replyContext, channel);
        } catch (Exception e) {
            throw PredefinedErrorCode.DECODE.fail(e, DefaultWrappedResponse.class, response.getClass());
        } finally {
            if (context != null) MetricsSupport.recordDeserializeEndTime(context);
        }
    }

    public void replyParser(ReplyParser<RESP> replyParser) {
        if (replyParser != null) {
            this.replyParser = replyParser;
        }
    }

    protected abstract Envelope.Request encodeRequest(WrappedRequest<Caller<?>> wrappedRequest, REQ request) throws Exception;
}
