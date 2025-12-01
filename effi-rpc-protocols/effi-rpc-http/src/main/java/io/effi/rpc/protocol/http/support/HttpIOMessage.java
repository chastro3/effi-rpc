package io.effi.rpc.protocol.http.support;


import io.effi.rpc.transport.endpoint.Channel;
import io.effi.rpc.transport.message.InputMessage;
import io.effi.rpc.transport.message.OutputMessage;
import io.netty.handler.codec.http.HttpHeaderNames;

import java.io.InputStream;
import java.io.OutputStream;

public class HttpIOMessage<SELF extends HttpIOMessage<SELF>> extends StandardHttpMessage
        implements InputMessage, OutputMessage {

    protected Channel channel;

    private InputStream inputStream;

    private OutputStream outputStream;

    protected HttpIOMessage(Builder<?, ?> builder) {
        super(builder);
    }

    @Override
    public SELF body(Object body) {
        this.body = body;
        return self();
    }

    public SELF channel(Channel channel) {
        this.channel = channel;
        return self();
    }


    public SELF input(InputStream inputStream) {
        this.inputStream = inputStream;
        return self();
    }

    public SELF output(OutputStream outputStream, int length) {
        this.outputStream = outputStream;
        setContentLength(length);
        return self();
    }

    @Override
    public Channel channel() {
        return channel;
    }

    @Override
    public InputStream inputStream() {
        return inputStream;
    }

    @Override
    public OutputStream outputStream() {
        return outputStream;
    }

    private void setContentLength(int length) {
        headers.add(HttpHeaderNames.CONTENT_LENGTH, String.valueOf(length));
    }

    @SuppressWarnings("unchecked")
    private SELF self() {
        return (SELF) this;
    }
}
