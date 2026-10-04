package io.effi.rpc.governance.registry;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.registry.RegistryConfig;
import io.effi.rpc.concurrent.Deadline;
import io.effi.rpc.concurrent.Future;
import io.effi.rpc.concurrent.Result;
import io.effi.rpc.config.SmartURL;
import io.effi.rpc.constant.Constant;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.InteractionErrorCodes;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.options.CallerOptions;
import io.effi.rpc.context.options.GovernanceOptions;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.logging.Logger;
import io.effi.rpc.logging.LoggerFactory;
import io.effi.rpc.registry.RegistryClient;
import io.effi.rpc.registry.ServiceInstance;
import io.effi.rpc.util.CollectionUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static io.effi.rpc.governance.registry.DefaultServiceDiscovery.NAME;


/**
 * Provides the default implementation of {@link ServiceDiscovery}.
 * <p>
 * Lookups are started for every registry configuration and share a single discovery deadline.
 * A failed registry is logged and skipped while other registries can still supply candidates.
 * Discovered instances are filtered by the request protocol and deduplicated by instance id.
 * Partial success is sufficient: registry failures are only reported when no instance can be
 * returned. When every registry succeeds but yields no match,
 * {@link InteractionErrorCodes#SERVICE_INSTANCE_NOT_FOUND} is reported; otherwise the last
 * observed registry failure is reported.
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
        Deadline deadline = discoveryDeadline(callTimeout, discoveryTimeout);
        Throwable lastFailure = null;
        RegistryConfig lastFailureRegistry = null;
        for (RegistryLookup lookup : lookups) {
            Result<List<ServiceInstance>> lookupResult;
            try {
                lookupResult = lookup.future().await(deadline);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw PredefinedErrorCode.REGISTRY_DISCOVER.fail(e, serviceName, lookup.registryConfig());
            }
            if (lookupResult.failed()) {
                lastFailure = lookupResult.cause();
                lastFailureRegistry = lookup.registryConfig();
                logger.warn("Failed to discover service '{}' from registry '{}'", lastFailure, serviceName, lastFailureRegistry);
                continue;
            }
            List<ServiceInstance> discoveredInstances = lookupResult.value();
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

    private Deadline discoveryDeadline(int callTimeout, int discoveryTimeout) {
        // A negative timeout means no deadline; otherwise the stricter of the two limits wins.
        long timeout = discoveryTimeout > 0
                ? (callTimeout > 0 ? Math.min(callTimeout, discoveryTimeout) : discoveryTimeout)
                : callTimeout;
        return timeout < 0 ? Deadline.none() : Deadline.after(timeout, TimeUnit.MILLISECONDS);
    }

    private record RegistryLookup(RegistryConfig registryConfig, Future<List<ServiceInstance>> future) {
    }
}
