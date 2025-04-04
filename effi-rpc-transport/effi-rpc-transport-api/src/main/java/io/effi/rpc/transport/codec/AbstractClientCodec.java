package io.effi.rpc.transport.codec;

import io.effi.rpc.common.exception.PredefinedErrorCode;
import io.effi.rpc.common.url.URL;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.ReplyFuture;
import io.effi.rpc.contract.Result;
import io.effi.rpc.contract.context.InvocationContext;
import io.effi.rpc.contract.context.ReplyContext;
import io.effi.rpc.contract.parameter.ReplyParser;
import io.effi.rpc.metrics.MetricsSupport;
import io.effi.rpc.transport.DefaultRepackagedResponse;
import io.effi.rpc.transport.RepackagedRequest;
import io.effi.rpc.transport.RepackagedResponse;
import io.effi.rpc.transport.endpoint.Channel;

/**
 * Abstract implementation of {@link ClientCodec}.
 *
 * @param <REQ>  the type of request
 * @param <RESP> the type of response
 */
public abstract class AbstractClientCodec<REQ extends Envelope.Request, RESP extends Envelope.Response> implements ClientCodec {

    protected ReplyParser<RESP> replyParser;

    @SuppressWarnings("unchecked")
    @Override
    public Envelope.Request encode(RepackagedRequest<Caller<?>> repackagedRequest) {
        REQ request = (REQ) repackagedRequest.request();
        if (!request.isInstance()) {
            return request;
        }
        MetricsSupport.recordSerializeStartTime(repackagedRequest.context());
        try {
            return encodeRequest(repackagedRequest, request);
        } catch (Exception e) {
            throw PredefinedErrorCode.ENCODE.fail(e, Envelope.Request.class, repackagedRequest.getClass());
        } finally {
            MetricsSupport.recordSerializeEndTime(repackagedRequest.context());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public RepackagedResponse<Caller<?>> decode(Channel channel, Envelope.Response response, ReplyFuture future) {
        InvocationContext<Envelope.Request, Caller<?>> context = future.context();
        try {
            URL requestUrl = response.url();
            MetricsSupport.recordDeserializeStartTime(context);
            Result result = null;
            if (!response.isInstance())
                result = replyParser.resolve((RESP) response, context.invoker());
            if (result == null) result = new Result(requestUrl, null);
            var replyContext = new ReplyContext<>(context, response, result);
            return new DefaultRepackagedResponse<>(replyContext, channel);
        } catch (Exception e) {
            throw PredefinedErrorCode.DECODE.fail(e, DefaultRepackagedResponse.class, response.getClass());
        } finally {
            if (context != null) MetricsSupport.recordDeserializeEndTime(context);
        }
    }

    public void replyParser(ReplyParser<RESP> replyParser) {
        if (replyParser != null) {
            this.replyParser = replyParser;
        }
    }

    protected abstract Envelope.Request encodeRequest(RepackagedRequest<Caller<?>> repackagedRequest, REQ request) throws Exception;
}
