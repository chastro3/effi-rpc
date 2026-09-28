package io.effi.rpc.protocol.http.h2;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.http2.DefaultHttp2DataFrame;
import io.netty.handler.codec.http2.Http2FrameStream;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Http2MessageStreamTest {

    @Test
    void rejectsDataBeyondConfiguredMessageSize() {
        Http2ResponseStream stream = new Http2ResponseStream(frameStream(), 4);
        DefaultHttp2DataFrame frame = new DefaultHttp2DataFrame(Unpooled.wrappedBuffer(new byte[5]), true);

        assertThrows(TooLongFrameException.class, () -> stream.parseDataFrame(frame));

        assertEquals(0, frame.refCnt());
        stream.close();
    }

    @Test
    void takeBodyTransfersBufferedDataExactlyOnce() {
        Http2ResponseStream stream = new Http2ResponseStream(frameStream(), 16);
        DefaultHttp2DataFrame frame = new DefaultHttp2DataFrame(Unpooled.wrappedBuffer(new byte[]{1, 2}), true);
        stream.parseDataFrame(frame);

        ByteBuf body = stream.takeBody();
        assertEquals(2, body.readableBytes());
        body.release();
    }

    private static Http2FrameStream frameStream() {
        return (Http2FrameStream) Proxy.newProxyInstance(
                Http2FrameStream.class.getClassLoader(),
                new Class<?>[]{Http2FrameStream.class},
                (proxy, method, args) -> {
                    if ("id".equals(method.getName())) {
                        return 1;
                    }
                    return defaultValue(method.getReturnType());
                }
        );
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive() || type == void.class) {
            return null;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == char.class) {
            return '\0';
        }
        if (type == byte.class) {
            return (byte) 0;
        }
        if (type == short.class) {
            return (short) 0;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == float.class) {
            return 0F;
        }
        return 0D;
    }
}
