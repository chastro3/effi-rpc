package io.effi.rpc.boot;

import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.registry.DefaultRegistryConfig;
import io.effi.rpc.component.registry.RegistryConfig;
import io.effi.rpc.concurrent.Future;
import io.effi.rpc.concurrent.Futures;
import io.effi.rpc.concurrent.Promise;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.protocol.http.h1.Http1ServerConfig;
import io.effi.rpc.registry.RegistryClient;
import io.effi.rpc.registry.ServiceInstance;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationServiceRegistrarTest {

    @Test
    void bindFailureKeepsRegistrarInactive() throws Exception {
        try (ServerSocket occupied = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            ScopedPlatform platform = new ScopedPlatform("bind-failure-platform");
            ScopedApplication application = platform.newApplication("bind-failure-application");
            ServerLauncher.attach(
                    application,
                    Http1ServerConfig.defaultConfig(),
                    occupied.getInetAddress().getHostAddress(),
                    occupied.getLocalPort()
            );
            ApplicationServiceRegistrar coordinator = new ApplicationServiceRegistrar(application);

            Future<Void> result = coordinator.register();

            assertThrows(CompletionException.class, () -> result.toCompletableFuture().join());
            assertFalse(coordinator.active());
        }
    }

    @Test
    void registerAfterCloseFails() throws Exception {
        ScopedPlatform platform = new ScopedPlatform("closed-platform");
        ScopedApplication application = platform.newApplication("closed-application");
        ApplicationServiceRegistrar coordinator = new ApplicationServiceRegistrar(application);

        coordinator.deregister();

        assertTrue(coordinator.register().await().failed());
        assertFalse(coordinator.active());
    }

    @Test
    void bootstrapStartFailureClosesApplication() throws Exception {
        try (ServerSocket occupied = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            ScopedPlatform platform = new ScopedPlatform("bootstrap-failure-platform");
            ScopedApplication application = platform.newApplication("bootstrap-failure-application");
            EffiRpcBootstrap bootstrap = EffiRpcBootstrap.newInstance(application)
                    .server(
                            Http1ServerConfig.defaultConfig(),
                            occupied.getInetAddress().getHostAddress(),
                            occupied.getLocalPort()
                    );

            Future<Void> startup = bootstrap.start();
            assertThrows(CompletionException.class, () -> startup.toCompletableFuture().join());

            assertFalse(application.active());
        }
    }

    @Test
    void registersAllServersInOneBatchPerRegistry() throws Exception {
        ScopedPlatform platform = new ScopedPlatform("batch-registration-platform");
        ScopedApplication application = platform.newApplication("batch-registration-application");
        String host = InetAddress.getLoopbackAddress().getHostAddress();
        ServerLauncher.attach(application, Http1ServerConfig.defaultConfig(), host, freePort());
        ServerLauncher.attach(application, Http1ServerConfig.defaultConfig(), host, freePort());
        BatchRegistryClient client = new BatchRegistryClient(platform);
        platform.registry().register(RegistryClient.Factory.class, "consul", client.factory());
        ApplicationServiceRegistrar registrar = new ApplicationServiceRegistrar(application);
        registrar.registry(DefaultRegistryConfig.builder()
                .type("consul")
                .address("consul://127.0.0.1:8500")
                .build());

        try {
            assertFalse(registrar.register().await().failed());
            assertEquals(1, client.batches.get());
            assertEquals(2, client.batchSize.get());
        } finally {
            registrar.deregister().await();
            platform.close();
        }
    }

    @Test
    void writesWeightToRegisteredInstances() throws Exception {
        ScopedPlatform platform = new ScopedPlatform("weight-registration-platform");
        ScopedApplication application = platform.newApplication("weight-registration-application");
        BatchRegistryClient client = new BatchRegistryClient(platform);
        platform.registry().register(RegistryClient.Factory.class, "consul", client.factory());
        ApplicationServiceRegistrar registrar = new ApplicationServiceRegistrar(application);
        registrar.server(
                Http1ServerConfig.defaultConfig(),
                InetAddress.getLoopbackAddress().getHostAddress(),
                freePort(),
                7
        );
        registrar.registry(DefaultRegistryConfig.builder()
                .type("consul")
                .address("consul://127.0.0.1:8500")
                .build());

        try {
            assertFalse(registrar.register().await().failed());
            assertEquals("7", client.registeredInstances.get(0).metadata().get(KeyConstant.WEIGHT));
        } finally {
            registrar.deregister().await();
            platform.close();
        }
    }

    private static int freePort() throws Exception {
        try (ServerSocket socket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            return socket.getLocalPort();
        }
    }

    private static final class BatchRegistryClient implements RegistryClient {

        private final ScopedPlatform platform;

        private final AtomicInteger batches = new AtomicInteger();

        private final AtomicInteger batchSize = new AtomicInteger();

        private final List<ServiceInstance> registeredInstances = new ArrayList<>();

        private BatchRegistryClient(ScopedPlatform platform) {
            this.platform = platform;
        }

        private RegistryClient.Factory factory() {
            return new RegistryClient.Factory() {
                @Override
                public RegistryClient fetch(RegistryConfig config) {
                    return BatchRegistryClient.this;
                }

                @Override
                public void clear() {
                }
            };
        }

        @Override
        public Future<Void> register(ServiceInstance instance) {
            throw new AssertionError("Single registration should be replaced by a batch");
        }

        @Override
        public Future<Void> register(Collection<ServiceInstance> instances) {
            batches.incrementAndGet();
            batchSize.addAndGet(instances.size());
            registeredInstances.addAll(instances);
            return Futures.completedVoid();
        }

        @Override
        public Future<Void> deregister(ServiceInstance instance) {
            return Futures.completedVoid();
        }

        @Override
        public Future<Void> deregister(Collection<ServiceInstance> instances) {
            return Futures.completedVoid();
        }

        @Override
        public Future<List<ServiceInstance>> lookup(String serviceName) {
            return Promise.completed(List.of());
        }

        @Override
        public ScopedPlatform platform() {
            return platform;
        }

        @Override
        public boolean active() {
            return true;
        }

        @Override
        public void close() {
        }
    }
}
