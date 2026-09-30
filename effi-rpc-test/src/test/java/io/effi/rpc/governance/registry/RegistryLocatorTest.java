package io.effi.rpc.governance.registry;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.registry.DefaultRegistryConfig;
import io.effi.rpc.component.registry.RegistryConfig;
import io.effi.rpc.concurrent.Future;
import io.effi.rpc.concurrent.Futures;
import io.effi.rpc.concurrent.Promise;
import io.effi.rpc.registry.RegistryClient;
import io.effi.rpc.registry.ServiceInstance;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

class RegistryLocatorTest {

    @Test
    void reusesLocatorForSamePlatformServiceAndConfigs() {
        ScopedPlatform platform = new ScopedPlatform("registry-locator-platform");
        RegistryConfig config = consulConfig();

        assertSame(
                RegistryLocator.cached(platform, "hello", config),
                RegistryLocator.cached(platform, "hello", config)
        );
        assertNotSame(
                RegistryLocator.cached(platform, "hello", config),
                RegistryLocator.cached(platform, "other", config)
        );
        assertNotSame(
                RegistryLocator.cached(platform, "hello", config),
                RegistryLocator.cached(new ScopedPlatform("registry-locator-other-platform"), "hello", config)
        );
    }

    @Test
    void preloadsDiscoveryOnceForSharedLocator() {
        ScopedPlatform platform = new ScopedPlatform("registry-locator-preload-platform");
        CountingRegistryClient client = new CountingRegistryClient(platform);
        platform.registry().register(RegistryClient.Factory.class, "consul", new RegistryClient.Factory() {
            @Override
            public RegistryClient fetch(RegistryConfig config) {
                return client;
            }

            @Override
            public void clear() {
            }
        });
        RegistryLocator locator = RegistryLocator.cached(platform, "hello", consulConfig());

        locator.preloadDiscoveries();
        locator.preloadDiscoveries();

        assertEquals(1, client.lookups.get());
    }

    private static RegistryConfig consulConfig() {
        return DefaultRegistryConfig.builder()
                .type("consul")
                .address("consul://127.0.0.1:8500")
                .build();
    }

    private static final class CountingRegistryClient implements RegistryClient {

        private final ScopedPlatform platform;

        private final AtomicInteger lookups = new AtomicInteger();

        private CountingRegistryClient(ScopedPlatform platform) {
            this.platform = platform;
        }

        @Override
        public Future<Void> register(ServiceInstance instance) {
            return Futures.completedVoid();
        }

        @Override
        public Future<Void> deregister(ServiceInstance instance) {
            return Futures.completedVoid();
        }

        @Override
        public Future<List<ServiceInstance>> lookup(String serviceName) {
            lookups.incrementAndGet();
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
