package io.effi.rpc.protocol.grpc;

import com.google.protobuf.StringValue;
import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.transport.ClientConfig;
import io.effi.rpc.component.transport.ProtocolStack;
import io.effi.rpc.component.transport.ServerConfig;
import io.effi.rpc.component.transport.support.DefaultClientConfig;
import io.effi.rpc.config.SmartURL;
import io.effi.rpc.context.options.CallerOptions;
import io.effi.rpc.context.options.GovernanceOptions;
import io.effi.rpc.context.options.PeerOptions;
import io.effi.rpc.core.DirectLocator;
import io.effi.rpc.core.InterfaceServantGroup;
import io.effi.rpc.option.HierarchicalOptions;
import io.effi.rpc.protocol.http.h2.Http2ServerConfig;
import io.effi.rpc.protocol.http.h1.Http1Protocol;
import io.effi.rpc.protocol.http.support.HttpDuplexRequest;
import io.effi.rpc.protocol.grpc.GrpcProtocol;
import io.effi.rpc.protocol.grpc.GrpcSupport;
import io.effi.rpc.protocol.grpc.client.GrpcCaller;
import io.effi.rpc.protocol.grpc.client.GrpcClient;
import io.effi.rpc.protocol.grpc.codec.GrpcFraming;
import io.effi.rpc.protocol.grpc.codec.GrpcHeaders;
import io.effi.rpc.protocol.grpc.codec.GrpcMessage;
import io.effi.rpc.protocol.grpc.codec.GrpcStatus;
import io.effi.rpc.protocol.grpc.stream.GrpcClientStream;
import io.effi.rpc.protocol.grpc.stream.StreamObserver;
import io.effi.rpc.serialization.Serializer;
import io.effi.rpc.transport.TransportProtocol;
import io.effi.rpc.transport.endpoint.Server;
import io.effi.rpc.transport.netty.NettyChannel;
import io.effi.rpc.util.TypeCapture;
import io.netty.handler.codec.http.HttpMethod;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GrpcEndToEndTest {

    private static final AtomicInteger PLATFORM_IDS = new AtomicInteger();

    private ScopedPlatform platform;

    private Server server;

    private GrpcClient client;

    private ScopedModule module;

    @AfterEach
    void tearDown() {
        if (client != null) {
            client.close();
        }
        if (server != null && server.active()) {
            server.close();
        }
        if (platform != null) {
            platform.close();
        }
    }

    @Test
    void unaryCallCrossesGrpcWireProtocol() throws Exception {
        GrpcClientStream stream = openStream(GreeterService.class.getName() + "/greet");
        Serializer serializer = serializer();
        List<StringValue> responses = new CopyOnWriteArrayList<>();
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch completed = new CountDownLatch(1);
        stream.subscribe(subscriber(serializer, responses, error, completed));

        stream.onNext(new GrpcMessage(false, GrpcSupport.serialize(serializer, StringValue.of("hello"))));
        stream.onComplete();

        assertTrue(completed.await(5, TimeUnit.SECONDS), "gRPC response was not received");
        assertNull(error.get(), () -> "gRPC call failed: " + error.get());
        assertEquals(1, responses.size());
        assertEquals("hello", responses.get(0).getValue());
    }

    @Test
    void serverStreamingCallDeliversMultipleMessages() throws Exception {
        GrpcClientStream stream = openStream(GreeterService.class.getName() + "/list");
        Serializer serializer = serializer();
        List<StringValue> responses = new CopyOnWriteArrayList<>();
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch completed = new CountDownLatch(1);
        stream.subscribe(subscriber(serializer, responses, error, completed));

        stream.onNext(new GrpcMessage(false, GrpcSupport.serialize(serializer, StringValue.of("tick"))));
        stream.onComplete();

        assertTrue(completed.await(5, TimeUnit.SECONDS), "gRPC response was not received");
        assertNull(error.get(), () -> "gRPC call failed: " + error.get());
        assertEquals(2, responses.size());
        assertEquals("tick", responses.get(0).getValue());
        assertEquals("tick", responses.get(1).getValue());
    }

    @Test
    void clientStreamingCallFeedsRequestObserver() throws Exception {
        GrpcClientStream stream = openStream(GreeterService.class.getName() + "/collect");
        Serializer serializer = serializer();
        List<StringValue> responses = new CopyOnWriteArrayList<>();
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch completed = new CountDownLatch(1);
        stream.subscribe(subscriber(serializer, responses, error, completed));

        stream.onNext(new GrpcMessage(false, GrpcSupport.serialize(serializer, StringValue.of("a"))));
        stream.onNext(new GrpcMessage(false, GrpcSupport.serialize(serializer, StringValue.of("b"))));
        stream.onComplete();

        assertTrue(completed.await(5, TimeUnit.SECONDS), "gRPC response was not received");
        assertNull(error.get(), () -> "gRPC call failed: " + error.get());
        assertEquals(1, responses.size());
        assertEquals("ab", responses.get(0).getValue());
    }

    @Test
    void bidirectionalCallStreamsBothDirections() throws Exception {
        GrpcClientStream stream = openStream(GreeterService.class.getName() + "/chat");
        Serializer serializer = serializer();
        List<StringValue> responses = new CopyOnWriteArrayList<>();
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch completed = new CountDownLatch(1);
        stream.subscribe(subscriber(serializer, responses, error, completed));

        stream.onNext(new GrpcMessage(false, GrpcSupport.serialize(serializer, StringValue.of("x"))));
        stream.onNext(new GrpcMessage(false, GrpcSupport.serialize(serializer, StringValue.of("y"))));
        stream.onComplete();

        assertTrue(completed.await(5, TimeUnit.SECONDS), "gRPC response was not received");
        assertNull(error.get(), () -> "gRPC call failed: " + error.get());
        assertEquals(2, responses.size());
        assertEquals("x", responses.get(0).getValue());
        assertEquals("y", responses.get(1).getValue());
    }

    @Test
    void grpcCallerUnaryCallReturnsReply() throws Exception {
        int port = prepareServer();
        HierarchicalOptions options = HierarchicalOptions.create()
                .addOption(CallerOptions.ENDPOINT, "127.0.0.1:" + port)
                .addOption(GovernanceOptions.LOCATOR, DirectLocator.Resolver.NAME)
                .addOption(PeerOptions.PATH, new String[]{GreeterService.class.getName() + "/greet"});
        TypeCapture<StringValue> replyType = TypeCapture.of(StringValue.class);
        GrpcCaller<StringValue> caller = GrpcCaller.builder(replyType)
                .options(options)
                .module(module)
                .build();

        StringValue reply = caller.unaryCall(StringValue.of("hello")).toCompletableFuture().join();

        assertEquals("hello", reply.getValue());
    }

    private GrpcClientStream openStream(String path) throws Exception {
        int port = prepareServer();
        TransportProtocol protocol = platform.namedExtension(TransportProtocol.class, GrpcProtocol.NAME);
        InetSocketAddress address = InetSocketAddress.createUnresolved("127.0.0.1", port);
        ClientConfig clientConfig = DefaultClientConfig.cached(GrpcProtocol.NAME, ProtocolStack.TCP);
        client = (GrpcClient) protocol.supplyClient(clientConfig, address, platform);
        NettyChannel channel = client.fetchChannel().await().value();
        GrpcClientStream stream = channel.channel().attr(GrpcSupport.CLIENT_STREAM).get();
        stream.start(request(path));
        return stream;
    }

    private static HttpDuplexRequest request(String path) {
        return HttpDuplexRequest.builder()
                .version(Http1Protocol.VERSION)
                .method(HttpMethod.POST)
                .url(SmartURL.valueOf("http://127.0.0.1:8080/" + path))
                .headers(List.of())
                .build();
    }

    private int prepareServer() throws Exception {
        platform = new ScopedPlatform("grpc-e2e-" + PLATFORM_IDS.incrementAndGet());
        ScopedApplication application = platform.newApplication("grpc-application");
        module = application.newModule("default");
        InterfaceServantGroup.<GreeterService>builder()
                .service(new GreeterServiceImpl())
                .targetType(GreeterService.class)
                .protocol(GrpcProtocol.NAME)
                .module(module)
                .build();

        TransportProtocol protocol = platform.namedExtension(TransportProtocol.class, GrpcProtocol.NAME);
        ServerConfig serverConfig = Http2ServerConfig.defaultConfig();
        server = protocol.supplyServer(
                serverConfig,
                InetSocketAddress.createUnresolved("127.0.0.1", 0),
                platform
        );
        NettyChannel bound = (NettyChannel) server.bind().await().value();
        return bound.localAddress().getPort();
    }

    private Serializer serializer() {
        return platform.preferredExtension(Serializer.class, "protobuf");
    }

    private Flow.Subscriber<GrpcMessage> subscriber(
            Serializer serializer,
            List<StringValue> responses,
            AtomicReference<Throwable> error,
            CountDownLatch completed
    ) {
        return new Flow.Subscriber<>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(GrpcMessage message) {
                responses.add((StringValue) GrpcSupport.deserialize(serializer, message.payload(), StringValue.class));
            }

            @Override
            public void onError(Throwable cause) {
                error.set(cause);
                completed.countDown();
            }

            @Override
            public void onComplete() {
                completed.countDown();
            }
        };
    }

    public interface GreeterService {

        StringValue greet(StringValue request);

        void list(StringValue request, StreamObserver<StringValue> observer);

        StreamObserver<StringValue> collect(StreamObserver<StringValue> observer);

        StreamObserver<StringValue> chat(StreamObserver<StringValue> observer);
    }

    public static final class GreeterServiceImpl implements GreeterService {

        @Override
        public StringValue greet(StringValue request) {
            return StringValue.of(request.getValue());
        }

        @Override
        public void list(StringValue request, StreamObserver<StringValue> observer) {
            observer.onNext(request);
            observer.onNext(request);
            observer.onCompleted();
        }

        @Override
        public StreamObserver<StringValue> collect(StreamObserver<StringValue> responseObserver) {
            return new StreamObserver<>() {
                private final StringBuilder values = new StringBuilder();

                @Override
                public void onNext(StringValue value) {
                    values.append(value.getValue());
                }

                @Override
                public void onError(Throwable cause) {
                    responseObserver.onError(cause);
                }

                @Override
                public void onCompleted() {
                    responseObserver.onNext(StringValue.of(values.toString()));
                    responseObserver.onCompleted();
                }
            };
        }

        @Override
        public StreamObserver<StringValue> chat(StreamObserver<StringValue> responseObserver) {
            return new StreamObserver<>() {
                @Override
                public void onNext(StringValue value) {
                    responseObserver.onNext(value);
                }

                @Override
                public void onError(Throwable cause) {
                    responseObserver.onError(cause);
                }

                @Override
                public void onCompleted() {
                    responseObserver.onCompleted();
                }
            };
        }
    }
}
