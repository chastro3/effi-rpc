package io.effi.rpc.registry;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.registry.DefaultRegistryConfig;
import io.effi.rpc.component.registry.RegistryConfig;
import io.effi.rpc.component.tools.Scheduler;
import io.effi.rpc.concurrent.Future;
import io.effi.rpc.concurrent.Futures;
import io.effi.rpc.concurrent.Promise;
import io.effi.rpc.exception.PredefinedErrorCode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbstractRegistryClientTest {

    @Test
    void retriesInitialRegistrationUntilSuccess() {
        ScopedPlatform platform = new ScopedPlatform("registry-retry-platform");
        platform.registry().register(Scheduler.class, new Scheduler());
        RegistryConfig config = DefaultRegistryConfig.builder()
                .type("test")
                .address("127.0.0.1:1")
                .retries(2)
                .heartbeatInterval(10)
                .build();
        TestRegistryClient client = new TestRegistryClient(config, platform);
        ServiceInstance instance = DefaultServiceInstance.builder()
                .id("instance-1")
                .serviceName("test-service")
                .protocol("http/1.1")
                .host("127.0.0.1")
                .port(8080)
                .build();

        Future<Void> result = client.register(instance);
        result.toCompletableFuture().join();

        assertEquals(2, client.attempts.get());
        assertEquals(1, client.registeredServiceInstances.get("test-service").size());
    }

    @Test
    void closeCancelsHeartbeatTasks() {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
        executor.setRemoveOnCancelPolicy(true);
        Scheduler scheduler = new Scheduler()
                .disposableService(executor)
                .periodicService(executor);
        ScopedPlatform platform = new ScopedPlatform("registry-close-platform");
        platform.registry().register(Scheduler.class, scheduler);
        RegistryConfig config = DefaultRegistryConfig.builder()
                .type("test")
                .address("127.0.0.1:1")
                .build();
        TestRegistryClient client = new TestRegistryClient(config, platform);
        ServiceInstance instance = instance("instance-close");

        client.register(instance).toCompletableFuture().join();
        assertFalse(executor.getQueue().isEmpty());

        client.close();

        assertTrue(executor.getQueue().isEmpty());
        scheduler.close();
    }

    @Test
    void lookupReturnsLatestSnapshotAfterSubscriptionUpdate() throws Exception {
        ScopedPlatform platform = new ScopedPlatform("registry-snapshot-platform");
        Scheduler scheduler = new Scheduler();
        platform.registry().register(Scheduler.class, scheduler);
        RegistryConfig config = DefaultRegistryConfig.builder()
                .type("test")
                .address("127.0.0.1:1")
                .build();
        TestRegistryClient client = new TestRegistryClient(config, platform);
        ServiceInstance first = instance("first");
        ServiceInstance second = instance("second");
        client.discovered = List.of(first, second);

        assertEquals(List.of(first, second), client.lookup("test-service").await().value());

        client.onServicesUpdated("test-service", List.of(first));

        assertEquals(List.of(first), client.lookup("test-service").await().value());
        client.close();
        scheduler.close();
    }

    @Test
    void closeShutsDownOwnedThreadPool() {
        ScopedPlatform platform = new ScopedPlatform("registry-owned-pool-platform");
        Scheduler scheduler = new Scheduler();
        platform.registry().register(Scheduler.class, scheduler);
        RegistryConfig config = DefaultRegistryConfig.builder()
                .type("test")
                .address("127.0.0.1:1")
                .build();
        TestRegistryClient client = new TestRegistryClient(config, platform);

        assertTrue(client.threadPool.active());

        client.close();

        assertFalse(client.threadPool.active());
        scheduler.close();
    }

    @Test
    void registerAllAndDeregisterApplyToEveryInstance() {
        ScopedPlatform platform = new ScopedPlatform("registry-batch-platform");
        platform.registry().register(Scheduler.class, new Scheduler());
        RegistryConfig config = DefaultRegistryConfig.builder()
                .type("test")
                .address("127.0.0.1:1")
                .retries(2)
                .heartbeatInterval(1)
                .build();
        TestRegistryClient client = new TestRegistryClient(config, platform);
        ServiceInstance first = instance("batch-first");
        ServiceInstance second = instance("batch-second");

        client.register(List.of(first, second)).toCompletableFuture().join();
        Set<ServiceInstance> registered = client.registeredServiceInstances.get("test-service");
        assertEquals(2, registered.size());

        client.deregister(List.of(first, second)).toCompletableFuture().join();
        assertTrue(registered.isEmpty());
    }

    private static ServiceInstance instance(String id) {
        return DefaultServiceInstance.builder()
                .id(id)
                .serviceName("test-service")
                .protocol("http/1.1")
                .host("127.0.0.1")
                .port(8080)
                .build();
    }

    private static final class TestRegistryClient extends AbstractRegistryClient {

        private final AtomicInteger attempts = new AtomicInteger();

        private List<ServiceInstance> discovered = List.of();

        private TestRegistryClient(RegistryConfig config, ScopedPlatform platform) {
            super(config, platform);
        }

        @Override
        public boolean active() {
            return true;
        }

        @Override
        protected Registration createRegistration(ServiceInstance instance) {
            return ignored -> {
                if (attempts.incrementAndGet() == 1) {
                    Promise<Void> failed = new Promise<>();
                    failed.failure(PredefinedErrorCode.COMMON.fail(
                            new IllegalStateException("first registration failed"),
                            "first registration failed"
                    ));
                    return failed;
                }
                return Futures.completedVoid();
            };
        }

        @Override
        protected void doSubscribe(String serviceName) {
        }

        @Override
        protected Future<Void> doDeregister(ServiceInstance instance) {
            return Futures.completedVoid();
        }

        @Override
        protected Future<List<ServiceInstance>> doLookup(String serviceName) {
            return Promise.completed(discovered);
        }

        @Override
        protected void doClose() {
        }
    }
}
