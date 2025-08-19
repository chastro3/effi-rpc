package io.effi.rpc.transport.message;

import io.effi.rpc.context.Interaction;
import io.effi.rpc.transport.codec.Encoder;
import io.effi.rpc.transport.endpoint.Channel;
import io.effi.rpc.util.AssertUtil;

import java.io.OutputStream;

/**
 * Wraps output messages with encoders and contexts for deferred encoding.
 * <p>
 * Provides encodable output message functionality that allows encoding
 * to be performed at appropriate times with encoder and context support.
 */
public final class EncodableOutputMessage<C extends Interaction.Context<?, ?>>
        extends AbstractIOMessage implements OutputMessage {

    private final C context;

    private final Encoder<C> encoder;

    private OutputMessage outputMessage;


    private EncodableOutputMessage(C context, Channel channel, Encoder<C> encoder) {
        super(context.message().url(), channel);
        this.encoder = encoder;
        this.context = context;
    }

    public static <C extends Interaction.Context<?, ?>> EncodableOutputMessage<C> create(C context, Channel channel, Encoder<C> encoder) {
        AssertUtil.notNull(context, "context");
        AssertUtil.notNull(encoder, "encoder");
        return new EncodableOutputMessage<>(context, channel, encoder);
    }

    public OutputMessage encode() {
        if (outputMessage == null) {
            outputMessage = encoder.encode(context, channel);
        }
        return outputMessage;
    }

    @Override
    public OutputStream outputStream() {
        return outputMessage == null ? null : outputMessage.outputStream();
    }

    public C context() {
        return context;
    }

}
