package io.effi.rpc.transport;

import io.effi.rpc.component.support.ThreadPool;
import io.effi.rpc.concurrent.Future;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.ReplyContext;
import io.effi.rpc.context.ReplyFuture;
import io.effi.rpc.context.Response;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.logging.Logger;
import io.effi.rpc.logging.LoggerFactory;
import io.effi.rpc.transport.codec.ClientExchangeContextCodec;
import io.effi.rpc.transport.message.InputMessage;

/**
 * Completes the reply future associated with one client-side response.
 */
public final class ClientResponseHandler {

    private static final Logger logger = LoggerFactory.getLogger(ClientResponseHandler.class);

    public void handle(InputMessage inputMessage) {
        ReplyFuture future = ReplyFuture.lookup(inputMessage.channel().platform(), inputMessage.url());
        if (future == null) {
            inputMessage.close();
            return;
        }
        Caller<?> caller = future.context().peer();
        try {
            if (TransportSupport.inIODeserialization(caller)) {
                completeAfterIoDecode(inputMessage, future, caller);
            } else {
                completeAfterPoolDecode(inputMessage, future, caller);
            }
        } catch (Throwable cause) {
            fail(inputMessage, future, cause);
        }
    }

    private void completeAfterIoDecode(InputMessage inputMessage, ReplyFuture future, Caller<?> caller) {
        ClientExchangeContextCodec clientCodec = inputMessage.channel().protocol().clientCodec();
        ReplyContext<Response, Caller<?>> replyContext = clientCodec.decode(inputMessage, caller);
        inputMessage.close();
        submit(inputMessage, future, caller.threadPool(), () -> future.complete(replyContext));
    }

    private void completeAfterPoolDecode(InputMessage inputMessage, ReplyFuture future, Caller<?> caller) {
        ClientExchangeContextCodec clientCodec = inputMessage.channel().protocol().clientCodec();
        submit(inputMessage, future, caller.threadPool(), () -> {
            ReplyContext<Response, Caller<?>> replyContext = clientCodec.decode(inputMessage, caller);
            future.complete(replyContext);
        });
    }

    private static void submit(InputMessage inputMessage, ReplyFuture future, ThreadPool threadPool, Runnable task) {
        Future<Void> delivery = threadPool.execute(task);
        delivery.onComplete(result -> {
            inputMessage.close();
            if (result.failed()) {
                fail(future, result.cause());
            }
        });
    }

    private static void fail(InputMessage inputMessage, ReplyFuture future, Throwable cause) {
        inputMessage.close();
        fail(future, cause instanceof EffiRpcException exception
                ? exception
                : TransportErrorCodes.CHANNEL_READ.fail(cause, inputMessage.channel().remoteAddress()));
    }

    private static void fail(ReplyFuture future, EffiRpcException cause) {
        future.failure(cause);
        logger.error(cause);
    }
}
