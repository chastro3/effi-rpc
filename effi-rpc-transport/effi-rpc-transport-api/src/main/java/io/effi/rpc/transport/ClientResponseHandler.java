package io.effi.rpc.transport;

import io.effi.rpc.component.support.ThreadPool;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.ReplyContext;
import io.effi.rpc.context.ReplyFuture;
import io.effi.rpc.context.Response;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.transport.codec.ClientExchangeContextCodec;
import io.effi.rpc.transport.endpoint.Channel;
import io.effi.rpc.transport.message.InputMessage;

/**
 * Completes the reply future associated with one client-side response.
 */
public final class ClientResponseHandler {

    private static final Logger logger = LoggerFactory.getLogger(ClientResponseHandler.class);

    public void handle(InputMessage inputMessage) {
        Channel channel = inputMessage.channel();
        ReplyFuture future = ReplyFuture.lookup(channel.platform(), inputMessage.url());
        if (future == null) {
            inputMessage.close();
            return;
        }

        ClientExchangeContextCodec clientCodec = channel.protocol().clientCodec();
        Caller<?> caller = future.context().peer();
        ThreadPool threadPool = caller.threadPool();
        try {
            if (TransportSupport.inIODeserialization(caller)) {
                ReplyContext<Response, Caller<?>> replyContext = clientCodec.decode(inputMessage, caller);
                inputMessage.close();
                threadPool.execute(() -> future.complete(replyContext))
                        .onComplete(result -> {
                            if (result.failed()) {
                                logger.error(result.cause());
                            }
                        });
            } else {
                threadPool.execute(() -> {
                    try {
                        ReplyContext<Response, Caller<?>> replyContext = clientCodec.decode(inputMessage, caller);
                        future.complete(replyContext);
                    } finally {
                        inputMessage.close();
                    }
                });
            }
        } catch (Exception cause) {
            inputMessage.close();
            EffiRpcException failure = TransportErrorCodes.CHANNEL_READ.fail(cause, channel.remoteAddress());
            threadPool.execute(() -> future.failure(failure));
        }
    }
}
