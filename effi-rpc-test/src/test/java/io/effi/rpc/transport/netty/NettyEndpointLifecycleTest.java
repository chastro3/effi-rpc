package io.effi.rpc.transport.netty;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.transport.ClientConfig;
import io.effi.rpc.component.transport.options.TransportOptions;
import io.effi.rpc.component.transport.support.DefaultClientConfig;
import io.effi.rpc.option.Options;
import io.effi.rpc.protocol.http.h1.Http1Client;
import io.effi.rpc.protocol.http.h1.Http1Protocol;
import io.effi.rpc.protocol.http.h1.Http1Server;
import io.effi.rpc.protocol.http.h1.Http1ServerConfig;
import io.effi.rpc.transport.TransportProtocol;
import io.effi.rpc.transport.endpoint.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NettyEndpointLifecycleTest {

    private static final AtomicInteger PLATFORM_IDS = new AtomicInteger();

    private ScopedPlatform platform;

    private Server server;

    private Http1Client client;

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
    void discardingPooledChannelReleasesPoolSlot() throws Exception {
        Http1Client client = start(5000, 6);
        NettyChannel channel = fetchChannel(client);
        assertTrue(channel.active());
        assertEquals(1, client.poolMetrics().acquired());

        client.discard(channel);

        await(() -> client.poolMetrics().acquired() == 0, "pool slot was not released");
        assertFalse(channel.active());
    }

    private Http1Client start(int idleTriggerInterval, int idleCountThreshold) throws Exception {
        platform = new ScopedPlatform("netty-lifecycle-" + PLATFORM_IDS.incrementAndGet());
        TransportProtocol protocol = platform.namedExtension(TransportProtocol.class, Http1Protocol.NAME);
        Http1ServerConfig serverConfig = Http1ServerConfig.builder()
                .idleTriggerInterval(idleTriggerInterval)
                .idleCountThreshold(idleCountThreshold)
                .build();
        server = new Http1Server(serverConfig, InetSocketAddress.createUnresolved("127.0.0.1", 0), platform);
        NettyChannel bound = (NettyChannel) server.bind().await().value();

        Options options = Options.create()
                .addOption(TransportOptions.IDLE_TRIGGER_INTERVAL, idleTriggerInterval)
                .addOption(TransportOptions.IDLE_COUNT_THRESHOLD, idleCountThreshold);
        ClientConfig clientConfig = new DefaultClientConfig(
                Http1Protocol.NAME,
                protocol.stack(),
                "netty-lifecycle-client",
                options,
                null
        );
        client = new Http1Client(
                clientConfig,
                InetSocketAddress.createUnresolved("127.0.0.1", bound.localAddress().getPort()),
                platform
        );
        return client;
    }

    private static NettyChannel fetchChannel(Http1Client client) throws InterruptedException {
        var result = client.fetchChannel().await();
        assertTrue(result.succeeded());
        return (NettyChannel) result.value();
    }

    private static void await(BooleanSupplier condition, String message) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!condition.getAsBoolean() && System.nanoTime() < deadline) {
            Thread.sleep(10);
        }
        assertTrue(condition.getAsBoolean(), message);
    }

    @Test
    void idleChannelClosesWithoutInboundTraffic() throws Exception {
        Http1Client client = start(100, 1);
        NettyChannel channel = fetchChannel(client);
        assertTrue(channel.active());

        await(() -> !channel.active(), "idle channel was not closed");
    }

    @Test
    void serverIsInactiveAfterClose() throws Exception {
        start(5000, 6);
        assertTrue(server.active());

        server.close();

        assertFalse(server.active());
        server = null;
    }
}
