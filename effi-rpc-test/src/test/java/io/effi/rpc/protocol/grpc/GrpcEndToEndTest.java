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
import io.effi.rpc.concurrent.Result;
import io.effi.rpc.context.InteractionErrorCodes;
import io.effi.rpc.context.options.CallerOptions;
import io.effi.rpc.context.options.GovernanceOptions;
import io.effi.rpc.context.options.PeerOptions;
import io.effi.rpc.core.DirectLocator;
import io.effi.rpc.core.InterfaceCallerGroup;
import io.effi.rpc.core.InterfaceServantGroup;
import io.effi.rpc.exception.EffiRpcException;
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
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpMethod;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.ArrayList;
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
        GrpcCaller<StringValue> caller = caller(port, "greet");

        StringValue reply = caller.unaryCall(StringValue.of("hello")).toCompletableFuture().join();

        assertEquals("hello", reply.getValue());
    }

    @Test
    void grpcCallerUnaryCallFailsWithMappedStatus() throws Exception {
        int port = prepareServer();
        GrpcCaller<StringValue> caller = caller(port, "fail");

        Result<StringValue> result = caller.unaryCall(StringValue.of("boom")).await();

        assertTrue(result.failed(), "gRPC call should fail");
        assertEquals(InteractionErrorCodes.SERVANT_INVOCATION_FAILED, result.cause().errorCode());
    }

    @Test
    void interfaceUnaryCallReturnsReply() throws Exception {
        GreeterService greeter = greeter();

        assertEquals("hello", greeter.greet(StringValue.of("hello")).getValue());
    }

    @Test
    void interfaceServerStreamingCallDeliversReplies() throws Exception {
        GreeterService greeter = greeter();
        List<StringValue> replies = new CopyOnWriteArrayList<>();
        CountDownLatch completed = new CountDownLatch(1);

        greeter.list(StringValue.of("tick"), observer(replies, completed));

        assertTrue(completed.await(5, TimeUnit.SECONDS), "gRPC replies were not received");
        assertEquals(2, replies.size());
        assertEquals("tick", replies.get(0).getValue());
        assertEquals("tick", replies.get(1).getValue());
    }

    @Test
    void interfaceClientStreamingCallCollectsRequests() throws Exception {
        GreeterService greeter = greeter();
        List<StringValue> replies = new CopyOnWriteArrayList<>();
        CountDownLatch completed = new CountDownLatch(1);

        StreamObserver<StringValue> requests = greeter.collect(observer(replies, completed));
        requests.onNext(StringValue.of("a"));
        requests.onNext(StringValue.of("b"));
        requests.onCompleted();

        assertTrue(completed.await(5, TimeUnit.SECONDS), "gRPC reply was not received");
        assertEquals(1, replies.size());
        assertEquals("ab", replies.get(0).getValue());
    }

    @Test
    void interfaceBidirectionalCallStreamsBothWays() throws Exception {
        GreeterService greeter = greeter();
        List<StringValue> replies = new CopyOnWriteArrayList<>();
        CountDownLatch completed = new CountDownLatch(1);

        StreamObserver<StringValue> requests = greeter.chat(observer(replies, completed));
        requests.onNext(StringValue.of("x"));
        requests.onNext(StringValue.of("y"));
        requests.onCompleted();

        assertTrue(completed.await(5, TimeUnit.SECONDS), "gRPC replies were not received");
        assertEquals(2, replies.size());
        assertEquals("x", replies.get(0).getValue());
        assertEquals("y", replies.get(1).getValue());
    }

    @Test
    void unknownServantFailsWithNotFound() throws Exception {
        GrpcClientStream stream = openStream(GreeterService.class.getName() + "/missing");
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch completed = new CountDownLatch(1);
        stream.subscribe(new Flow.Subscriber<>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(GrpcMessage message) {
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
        });

        assertTrue(completed.await(5, TimeUnit.SECONDS), "gRPC failure was not received");
        assertTrue(error.get() instanceof EffiRpcException, "expected an EffiRpcException failure");
        assertEquals(InteractionErrorCodes.SERVANT_NOT_FOUND, ((EffiRpcException) error.get()).errorCode());
    }

    @Test
    void concurrentStreamsShareOneConnection() throws Exception {
        int port = prepareServer();
        TransportProtocol protocol = platform.namedExtension(TransportProtocol.class, GrpcProtocol.NAME);
        ClientConfig clientConfig = DefaultClientConfig.cached(GrpcProtocol.NAME, ProtocolStack.TCP);
        client = (GrpcClient) protocol.supplyClient(
                clientConfig,
                InetSocketAddress.createUnresolved("127.0.0.1", port),
                platform
        );
        Serializer serializer = serializer();
        int calls = 8;
        List<GrpcClientStream> streams = new ArrayList<>();
        Channel connection = null;
        for (int i = 0; i < calls; i++) {
            NettyChannel channel = client.fetchChannel().await().value();
            connection = connection == null ? channel.channel().parent() : connection;
            assertEquals(connection, channel.channel().parent(), "all streams must share one connection");
            streams.add(channel.channel().attr(GrpcSupport.CLIENT_STREAM).get());
        }

        List<List<StringValue>> responses = new ArrayList<>(calls);
        List<CountDownLatch> completed = new ArrayList<>(calls);
        for (GrpcClientStream stream : streams) {
            List<StringValue> values = new CopyOnWriteArrayList<>();
            CountDownLatch latch = new CountDownLatch(1);
            responses.add(values);
            completed.add(latch);
            stream.subscribe(subscriber(serializer, values, new AtomicReference<>(), latch));
        }
        for (GrpcClientStream stream : streams) {
            stream.start(request(GreeterService.class.getName() + "/greet"));
            stream.onNext(new GrpcMessage(false, GrpcSupport.serialize(serializer, StringValue.of("hello"))));
            stream.onComplete();
        }

        for (int i = 0; i < calls; i++) {
            assertTrue(completed.get(i).await(5, TimeUnit.SECONDS), "stream " + i + " did not complete");
            assertEquals(1, responses.get(i).size());
            assertEquals("hello", responses.get(i).get(0).getValue());
        }
    }

    private GrpcCaller<StringValue> caller(int port, String method) {
        HierarchicalOptions options = HierarchicalOptions.create()
                .addOption(CallerOptions.ENDPOINT, "127.0.0.1:" + port)
                .addOption(GovernanceOptions.LOCATOR, DirectLocator.Resolver.NAME)
                .addOption(PeerOptions.PATH, new String[]{GreeterService.class.getName() + "/" + method});
        TypeCapture<StringValue> replyType = TypeCapture.of(StringValue.class);
        return GrpcCaller.builder(replyType)
                .options(options)
                .module(module)
                .build();
    }

    private GreeterService greeter() throws Exception {
        int port = prepareServer();
        HierarchicalOptions options = HierarchicalOptions.create()
                .addOption(CallerOptions.ENDPOINT, "127.0.0.1:" + port)
                .addOption(GovernanceOptions.LOCATOR, DirectLocator.Resolver.NAME);
        return InterfaceCallerGroup.<GreeterService>builder()
                .targetType(GreeterService.class)
                .protocol(GrpcProtocol.NAME)
                .options(options)
                .module(module)
                .build()
                .proxy();
    }

    private StreamObserver<StringValue> observer(List<StringValue> replies, CountDownLatch completed) {
        return new StreamObserver<>() {
            @Override
            public void onNext(StringValue value) {
                replies.add(value);
            }

            @Override
            public void onError(Throwable cause) {
                completed.countDown();
            }

            @Override
            public void onCompleted() {
                completed.countDown();
            }
        };
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

        StringValue fail(StringValue request);

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
        public StringValue fail(StringValue request) {
            throw new IllegalStateException("boom");
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
