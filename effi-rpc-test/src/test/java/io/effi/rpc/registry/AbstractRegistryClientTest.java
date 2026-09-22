package io.effi.rpc.registry;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.registry.DefaultRegistryConfig;
import io.effi.rpc.component.registry.RegistryConfig;
import io.effi.rpc.component.support.Scheduler;
import io.effi.rpc.concurrent.Future;
import io.effi.rpc.concurrent.Promise;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    private static final class TestRegistryClient extends AbstractRegistryClient {

        private final AtomicInteger attempts = new AtomicInteger();

        private TestRegistryClient(RegistryConfig config, ScopedPlatform platform) {
            super(config, platform, false);
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
                    failed.failure(new IllegalStateException("first registration failed"));
                    return failed;
                }
                return Promise.completedVoid();
            };
        }

        @Override
        protected void doSubscribe(String serviceName) {
        }

        @Override
        protected Future<Void> doDeregister(ServiceInstance instance) {
            return Promise.completedVoid();
        }

        @Override
        protected Future<List<ServiceInstance>> doLookup(String serviceName) {
            return Promise.completed(Collections.emptyList());
        }

        @Override
        protected void doClose() {
        }
    }
}
