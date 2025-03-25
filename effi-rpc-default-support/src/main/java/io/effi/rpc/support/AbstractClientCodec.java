package io.effi.rpc.support;

import io.effi.rpc.common.exception.EffiRpcException;
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
import io.effi.rpc.transport.NettyChannel;
import io.effi.rpc.transport.RequestWrapper;
import io.effi.rpc.transport.ResponseWrapper;
import io.effi.rpc.transport.codec.ClientCodec;

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
    public Envelope.Request encode(NettyChannel channel, RequestWrapper<Caller<?>> requestWrapper) {
        REQ request = (REQ) requestWrapper.request();
        if (!request.isInstance()) {
            return request;
        }
        var context = requestWrapper.context();
        MetricsSupport.recordSerializeStartTime(context);
        try {
            return encodeRequest(requestWrapper, request);
        } catch (Exception e) {
            throw EffiRpcException.wrap(PredefinedErrorCode.ENCODE, e, Envelope.Request.class,
                    requestWrapper.request().getClass());
        } finally {
            MetricsSupport.recordSerializeEndTime(context);
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public ResponseWrapper<Caller<?>> decode(NettyChannel channel, Envelope.Response response) {
        InvocationContext<Envelope.Request, Caller<?>> context = null;
        try {
            URL requestUrl = response.url();
            ReplyFuture future = ReplyFuture.acquireFuture(requestUrl);
            if (future == null) return null;
            context = future.context();
            MetricsSupport.recordDeserializeStartTime(context);
            Result result = null;
            if (!response.isInstance())
                result = replyParser.resolve((RESP) response, context.invoker());
            if (result == null) result = new Result(requestUrl, null);
            var replyContext = new ReplyContext<>(context, response, result);
            return ResponseWrapper.parse(replyContext);
        } catch (Exception e) {
            throw EffiRpcException.wrap(PredefinedErrorCode.ENCODE, e, response.getClass(), ResponseWrapper.class);
        } finally {
            if (context != null) MetricsSupport.recordDeserializeEndTime(context);
        }
    }

    /**
     * Sets the replyParser.
     *
     * @param replyParser replyParser
     */
    public void replyParser(ReplyParser<RESP> replyParser) {
        if (replyParser != null) {
            this.replyParser = replyParser;
        }
    }

    protected abstract Envelope.Request encodeRequest(RequestWrapper<Caller<?>> requestWrapper, REQ request) throws Exception;
}
