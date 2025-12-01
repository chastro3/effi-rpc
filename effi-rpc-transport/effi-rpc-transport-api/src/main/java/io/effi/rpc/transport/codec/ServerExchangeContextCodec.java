package io.effi.rpc.transport.codec;

import io.effi.rpc.context.Servant;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.ReplyContext;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.Response;
import io.effi.rpc.transport.endpoint.Channel;
import io.effi.rpc.transport.message.InputMessage;
import io.effi.rpc.transport.message.OutputMessage;

/**
 * Combines encoding and decoding operations for server exchange contexts.
 * <p>
 * Provides codec functionality for converting {@link ReplyContext} to {@link OutputMessage}
 * and {@link InputMessage} to {@link CallContext} in server-side communication.
 */
public interface ServerExchangeContextCodec
        extends Encoder<ReplyContext<Response, Servant>>,
        Decoder<CallContext<Request, Servant>, Servant> {

    /**
     * Encodes the given reply context into an output message using the specified channel.
     *
     * @param context the reply context to encode
     * @param channel the associated channel
     * @return the encoded output message
     */
    @Override
    OutputMessage encode(ReplyContext<Response, Servant> context, Channel channel);

    /**
     * Decodes the input message into a call context using the given callee.
     *
     * @param inputMessage the message to decode
     * @param servant       the associated callee
     * @return the decoded call context
     */
    @Override
    CallContext<Request, Servant> decode(InputMessage inputMessage, Servant servant);
}


