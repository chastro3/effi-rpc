package io.effi.rpc.protocol.grpc;

import io.effi.rpc.protocol.grpc.codec.GrpcFraming;
import io.effi.rpc.protocol.grpc.codec.GrpcMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.TooLongFrameException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GrpcFramingTest {

    @Test
    void roundTripsMessages() {
        ByteBuf frame = GrpcFraming.encode(ByteBufAllocator.DEFAULT, false, new byte[]{1, 2, 3});
        try {
            List<GrpcMessage> messages = new GrpcFraming.Decoder(1024).decode(frame);

            assertEquals(1, messages.size());
            assertFalse(messages.get(0).compressed());
            assertArrayEquals(new byte[]{1, 2, 3}, messages.get(0).payload());
        } finally {
            frame.release();
        }
    }

    @Test
    void decodesAcrossDataFrames() {
        ByteBuf frame = GrpcFraming.encode(ByteBufAllocator.DEFAULT, true, new byte[]{9, 8, 7, 6});
        ByteBuf first = frame.copy(0, 4);
        ByteBuf second = frame.copy(4, frame.readableBytes() - 4);
        try {
            GrpcFraming.Decoder decoder = new GrpcFraming.Decoder(1024);
            assertTrue(decoder.decode(first).isEmpty());

            List<GrpcMessage> messages = decoder.decode(second);
            assertEquals(1, messages.size());
            assertTrue(messages.get(0).compressed());
            assertArrayEquals(new byte[]{9, 8, 7, 6}, messages.get(0).payload());
        } finally {
            first.release();
            second.release();
            frame.release();
        }
    }

    @Test
    void decodesMultipleMessagesFromOneBuffer() {
        ByteBuf first = GrpcFraming.encode(ByteBufAllocator.DEFAULT, false, new byte[]{1});
        ByteBuf second = GrpcFraming.encode(ByteBufAllocator.DEFAULT, false, new byte[]{2});
        ByteBuf combined = Unpooled.wrappedBuffer(first, second);
        try {
            List<GrpcMessage> messages = new GrpcFraming.Decoder(1024).decode(combined);

            assertEquals(2, messages.size());
            assertArrayEquals(new byte[]{1}, messages.get(0).payload());
            assertArrayEquals(new byte[]{2}, messages.get(1).payload());
        } finally {
            combined.release();
        }
    }

    @Test
    void rejectsOversizeMessages() {
        ByteBuf frame = GrpcFraming.encode(ByteBufAllocator.DEFAULT, false, new byte[8]);
        try {
            assertThrows(TooLongFrameException.class, () -> new GrpcFraming.Decoder(4).decode(frame));
        } finally {
            frame.release();
        }
    }
}
