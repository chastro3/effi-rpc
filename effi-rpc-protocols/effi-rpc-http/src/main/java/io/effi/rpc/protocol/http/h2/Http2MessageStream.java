package io.effi.rpc.protocol.http.h2;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http2.*;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Handle http2 frames,Then generate full http2 stream message.
 */
public abstract class Http2MessageStream {

    protected final Http2FrameStream stream;

    protected final Http2Headers headers;

    protected CompositeByteBuf compositeByteBuf;

    protected AtomicBoolean endStream;

    public Http2MessageStream(Http2FrameStream stream) {
        this.stream = stream;
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
        writeData(byteBuf);
        if (dataFrame.isEndStream()) end();

    }

    private void writeData(ByteBuf byteBuf) {
        if (byteBuf == null || !byteBuf.isReadable()) {
            return;
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

    /**
     * Returns the current body.
     *
     * @return
     */
    public ByteBuf body() {
        if (!endStream.get()) {
            throw new IllegalStateException("Stream is not end");
        }
        return compositeByteBuf == null ? Unpooled.EMPTY_BUFFER : compositeByteBuf;
    }

    /**
     * Check whether the current HTTP 2 stream has been read.
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
