package io.effi.rpc.protocol.http.h2;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http2.*;
import io.netty.handler.codec.TooLongFrameException;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Handle http2 frames,Then generate full http2 stream 
 */
public abstract class Http2MessageStream {

    protected final Http2FrameStream stream;

    protected final Http2Headers headers;

    protected CompositeByteBuf compositeByteBuf;

    protected final AtomicBoolean endStream;

    private final int maxMessageSize;

    public Http2MessageStream(Http2FrameStream stream, int maxMessageSize) {
        this.stream = stream;
        this.maxMessageSize = maxMessageSize;
        headers = new DefaultHttp2Headers();
        endStream = new AtomicBoolean(false);
    }

    /**
     * Parse header frame.
     *
     * @param headersFrame
     */
    public void parseHeaderFrame(Http2HeadersFrame headersFrame) {
        headers.add(headersFrame.headers());
        if (headersFrame.isEndStream()) end();
    }

    /**
     * Parse data frame.
     *
     * @param dataFrame
     */
    public void parseDataFrame(Http2DataFrame dataFrame) {
        ByteBuf byteBuf = dataFrame.content();
        try {
            if (!byteBuf.isReadable()) {
                dataFrame.release();
            } else {
                writeData(byteBuf);
            }
        } catch (RuntimeException e) {
            dataFrame.release();
            throw e;
        }
        if (dataFrame.isEndStream()) end();
    }

    private void writeData(ByteBuf byteBuf) {
        int readableBytes = byteBuf.readableBytes();
        int accumulated = compositeByteBuf == null ? 0 : compositeByteBuf.readableBytes();
        if (readableBytes > maxMessageSize - accumulated) {
            throw new TooLongFrameException(
                    "HTTP/2 message exceeds the configured maximum size of " + maxMessageSize + " bytes"
            );
        }
        if (compositeByteBuf == null) {
            compositeByteBuf = ByteBufAllocator.DEFAULT.compositeBuffer();
        }
        compositeByteBuf.addComponent(true, byteBuf);
    }

    /**
     * Returns the current http2 frame stream.
     *
     * @return
     */
    public Http2FrameStream stream() {
        return stream;
    }

    /**
     * Returns the current headers.
     *
     * @return
     */
    public Http2Headers headers() {
        return headers;
    }

    public ByteBuf takeBody() {
        if (!endStream.get()) {
            throw new IllegalStateException("Stream is not end");
        }
        ByteBuf body = compositeByteBuf == null ? Unpooled.EMPTY_BUFFER : compositeByteBuf;
        compositeByteBuf = null;
        return body;
    }

    public void close() {
        if (compositeByteBuf != null) {
            compositeByteBuf.release();
            compositeByteBuf = null;
        }
    }

    /**
     * Check if the current HTTP 2 stream has been read.
     *
     * @return
     */
    public boolean endStream() {
        return endStream.get();
    }

    /**
     * The current stream read has ended.
     */
    protected void end() {
        endStream.compareAndSet(false, true);
    }
}
