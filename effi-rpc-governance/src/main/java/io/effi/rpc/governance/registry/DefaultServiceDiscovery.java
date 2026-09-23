package io.effi.rpc.governance.registry;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.registry.RegistryConfig;
import io.effi.rpc.config.SmartURL;
import io.effi.rpc.constant.Constant;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.InteractionErrorCodes;
import io.effi.rpc.context.Request;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.registry.RegistryClient;
import io.effi.rpc.registry.ServiceInstance;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.concurrent.Future;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static io.effi.rpc.governance.registry.DefaultServiceDiscovery.NAME;
import io.effi.rpc.context.options.CallerOptions;
import io.effi.rpc.context.options.GovernanceOptions;


/**
 * Provides the default implementation of {@link ServiceDiscovery}.
 * <p>Deduplication based on address.</p>
 */
@Extension(value = NAME, primary = true)
public class DefaultServiceDiscovery implements ServiceDiscovery {

    public static final String NAME = Constant.DEFAULT_NAME;

    private static final Logger logger = LoggerFactory.getLogger(DefaultServiceDiscovery.class);

    @Override
    public List<ServiceInstance> discover(String serviceName, CallContext<Request, Caller<?>> context, Collection<RegistryConfig> registryConfigs) {
        List<ServiceInstance> availableInstances = new ArrayList<>();
        SmartURL smartUrl = context.message().url();
        int size = registryConfigs.size();
        ScopedPlatform platform = context.module().platform();
        List<RegistryLookup> lookups = new ArrayList<>(size);
        for (RegistryConfig registryConfig : registryConfigs) {
            RegistryClient registryClient = RegistryClient.of(registryConfig, platform);
            lookups.add(new RegistryLookup(registryConfig, registryClient.lookup(serviceName)));
        }
        int callTimeout = context.peer().option(CallerOptions.TIMEOUT);
        int discoveryTimeout = context.peer().option(GovernanceOptions.SERVICE_DISCOVERY_TIMEOUT);
        long timeout = Math.max(1, discoveryTimeout > 0 ? Math.min(callTimeout, discoveryTimeout) : callTimeout);
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeout);
        Throwable lastFailure = null;
        RegistryConfig lastFailureRegistry = null;
        for (RegistryLookup lookup : lookups) {
            List<ServiceInstance> discoveredInstances;
            try {
                long remaining = deadline - System.nanoTime();
                if (remaining <= 0) {
                    throw new TimeoutException("Service discovery timed out");
                }
                discoveredInstances = lookup.future().toCompletableFuture().get(remaining, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw PredefinedErrorCode.REGISTRY_DISCOVER.fail(e, serviceName, lookup.registryConfig());
            } catch (ExecutionException | TimeoutException e) {
                lastFailure = e instanceof ExecutionException && e.getCause() != null ? e.getCause() : e;
                lastFailureRegistry = lookup.registryConfig();
                logger.warn("Failed to discover service '{}' from registry '{}'", lastFailure, serviceName, lastFailureRegistry);
                continue;
            }
            if (CollectionUtil.isNotEmpty(discoveredInstances)) {
                for (ServiceInstance discoveredInstance : discoveredInstances) {
                    if (discoveredInstance.protocol().equals(smartUrl.scheme())) {
                        CollectionUtil.addToList(
                                availableInstances,
                                (existedInst, newInst) -> Objects.equals(existedInst.id(), newInst.id()),
                                discoveredInstance);
                    }
                }
            }
        }
        if (availableInstances.isEmpty()) {
            if (lastFailure != null) {
                throw PredefinedErrorCode.REGISTRY_DISCOVER.fail(lastFailure, serviceName, lastFailureRegistry);
            }
            throw InteractionErrorCodes.SERVICE_INSTANCE_NOT_FOUND.fail(serviceName);
        }
        return availableInstances;
    }

    private record RegistryLookup(RegistryConfig registryConfig, Future<List<ServiceInstance>> future) {
    }
}
