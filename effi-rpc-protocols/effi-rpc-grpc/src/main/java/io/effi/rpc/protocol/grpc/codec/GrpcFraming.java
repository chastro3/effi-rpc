package io.effi.rpc.protocol.grpc.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.TooLongFrameException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Provides gRPC length-prefixed message encoding and decoding.
 */
public final class GrpcFraming {

    public static final int PREFIX_LENGTH = 5;

    private GrpcFraming() {
    }

    /**
     * Encodes one gRPC message into a length-prefixed frame.
     *
     * @param allocator  buffer allocator
     * @param compressed whether the payload uses message compression
     * @param payload    serialized payload
     * @return encoded frame
     */
    public static ByteBuf encode(ByteBufAllocator allocator, boolean compressed, byte[] payload) {
        ByteBuf frame = allocator.buffer(PREFIX_LENGTH + payload.length);
        frame.writeByte(compressed ? 1 : 0);
        frame.writeInt(payload.length);
        frame.writeBytes(payload);
        return frame;
    }

    /**
     * Decodes complete gRPC messages from a byte stream.
     */
    public static final class Decoder {

        private final int maxMessageSize;

        private byte[] buffer = new byte[PREFIX_LENGTH];

        private int size;

        public Decoder(int maxMessageSize) {
            this.maxMessageSize = maxMessageSize <= 0 ? Integer.MAX_VALUE : maxMessageSize;
        }

        /**
         * Appends bytes and returns every newly completed message.
         *
         * @param data inbound bytes
         * @return completed messages
         */
        public List<GrpcMessage> decode(ByteBuf data) {
            append(data);
            List<GrpcMessage> messages = new ArrayList<>();
            int offset = 0;
            while (size - offset >= PREFIX_LENGTH) {
                int compressed = buffer[offset] & 0xFF;
                long length = ((long) (buffer[offset + 1] & 0xFF) << 24)
                        | ((long) (buffer[offset + 2] & 0xFF) << 16)
                        | ((long) (buffer[offset + 3] & 0xFF) << 8)
                        | (buffer[offset + 4] & 0xFF);
                if (length > maxMessageSize) {
                    throw new TooLongFrameException(
                            "gRPC message exceeds the configured maximum size of " + maxMessageSize + " bytes"
                    );
                }
                long frameLength = PREFIX_LENGTH + length;
                if (size - offset < frameLength) {
                    break;
                }
                byte[] payload = Arrays.copyOfRange(buffer, offset + PREFIX_LENGTH, offset + (int) frameLength);
                messages.add(new GrpcMessage(compressed != 0, payload));
                offset += (int) frameLength;
            }
            compact(offset);
            return messages;
        }

        private void append(ByteBuf data) {
            int readable = data.readableBytes();
            ensure(size + readable);
            data.readBytes(buffer, size, readable);
            size += readable;
        }

        private void ensure(int capacity) {
            if (capacity <= buffer.length) {
                return;
            }
            int newCapacity = buffer.length;
            while (newCapacity < capacity) {
                newCapacity <<= 1;
            }
            buffer = Arrays.copyOf(buffer, newCapacity);
        }

        private void compact(int offset) {
            if (offset == 0) {
                return;
            }
            int remaining = size - offset;
            System.arraycopy(buffer, offset, buffer, 0, remaining);
            size = remaining;
        }
    }
}
