package io.effi.rpc.transport.codec;

import io.effi.rpc.context.Caller;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.ReplyContext;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.Response;
import io.effi.rpc.transport.endpoint.Channel;
import io.effi.rpc.transport.message.InputMessage;
import io.effi.rpc.transport.message.OutputMessage;

/**
 * Combines encoding and decoding operations for client exchange contexts.
 * <p>
 * Provides codec functionality for converting {@link CallContext} to {@link OutputMessage}
 * and {@link InputMessage} to {@link ReplyContext} in client-side communication.
 */
public interface ClientExchangeContextCodec
        extends Encoder<CallContext<Request, Caller<?>> >,
        Decoder<ReplyContext<Response, Caller<?>>, Caller<?>> {

    /**
     * Encodes the given call context into an output message using the specified channel.
     *
     * @param context the call context to encode
     * @param channel the associated channel
     * @return the encoded output message
     */
    @Override
    OutputMessage encode(CallContext<Request, Caller<?>> context, Channel channel);

    /**
     * Decodes the input message into a reply context using the given caller.
     *
     * @param inputMessage the message to decode
     * @param caller       the associated caller
     * @return the decoded reply context
     */
    @Override
    ReplyContext<Response, Caller<?>> decode(InputMessage inputMessage, Caller<?> caller);
}






