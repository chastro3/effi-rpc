package io.effi.rpc.protocol.grpc;

import io.effi.rpc.config.SmartURL;
import io.effi.rpc.protocol.http.h1.Http1Protocol;
import io.effi.rpc.protocol.http.support.HttpDuplexRequest;
import io.effi.rpc.protocol.grpc.codec.GrpcFraming;
import io.effi.rpc.protocol.grpc.codec.GrpcHeaders;
import io.effi.rpc.protocol.grpc.codec.GrpcMessage;
import io.effi.rpc.protocol.grpc.codec.GrpcStatus;
import io.effi.rpc.protocol.grpc.stream.GrpcClientStream;
import io.effi.rpc.protocol.grpc.stream.GrpcServerStream;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http2.Http2DataFrame;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GrpcStreamTest {

    @Test
    void clientStreamWritesFramedMessagesAndHalfCloses() throws Exception {
        EmbeddedChannel channel = new EmbeddedChannel();
        try {
            GrpcClientStream stream = new GrpcClientStream(channel);
            stream.start(request("test.Service/Call"));
            Http2HeadersFrame headers = channel.readOutbound();
            assertEquals("test.Service/Call", headers.headers().path().toString());

            stream.send(new GrpcMessage(false, new byte[]{1, 2, 3})).await();
            Http2DataFrame data = channel.readOutbound();
            List<GrpcMessage> messages = new GrpcFraming.Decoder(1024).decode(data.content());
            assertEquals(1, messages.size());
            assertArrayEquals(new byte[]{1, 2, 3}, messages.get(0).payload());
            data.release();

            stream.onComplete();
            Http2DataFrame end = channel.readOutbound();
            assertTrue(end.isEndStream());
            end.release();
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    void serverStreamWritesStatusTrailersOnCompletion() {
        EmbeddedChannel channel = new EmbeddedChannel();
        try {
            GrpcServerStream stream = new GrpcServerStream(channel);
            stream.start();
            Http2HeadersFrame headers = channel.readOutbound();
            assertEquals("200", headers.headers().status().toString());

            stream.onComplete();
            Http2HeadersFrame trailers = channel.readOutbound();
            assertTrue(trailers.isEndStream());
            assertEquals(String.valueOf(GrpcStatus.OK.code()), trailers.headers().get(GrpcHeaders.STATUS).toString());
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    void deliversInboundMessagesAndCompletion() {
        EmbeddedChannel channel = new EmbeddedChannel();
        try {
            GrpcClientStream stream = new GrpcClientStream(channel);
            List<GrpcMessage> received = new ArrayList<>();
            boolean[] completed = {false};
            stream.subscribe(new Flow.Subscriber<>() {
                @Override
                public void onSubscribe(Flow.Subscription subscription) {
                    subscription.request(Long.MAX_VALUE);
                }

                @Override
                public void onNext(GrpcMessage item) {
                    received.add(item);
                }

                @Override
                public void onError(Throwable throwable) {
                }

                @Override
                public void onComplete() {
                    completed[0] = true;
                }
            });

            stream.inboundMessage(new GrpcMessage(false, new byte[]{9}));
            stream.inboundCompleted();
            channel.runPendingTasks();

            assertEquals(1, received.size());
            assertTrue(completed[0]);
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    private static HttpDuplexRequest request(String path) {
        return HttpDuplexRequest.builder()
                .version(Http1Protocol.VERSION)
                .method(HttpMethod.POST)
                .url(SmartURL.valueOf("http://127.0.0.1:8080/" + path))
                .headers(List.of())
                .build();
    }
}
