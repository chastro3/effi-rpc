package io.effi.rpc.registry;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.registry.RegistryConfig;
import io.effi.rpc.component.support.Scheduler;
import io.effi.rpc.component.support.ThreadPool;
import io.effi.rpc.executor.RpcThreadPool;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.concurrent.Future;
import io.effi.rpc.concurrent.Promise;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Provides an abstract implementation of {@link RegistryClient}.
 */
public abstract class AbstractRegistryClient implements RegistryClient {

    protected static final String REGISTRY = "registry";

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final Map<String, DiscoveredService> subscribedHealthServices = new ConcurrentHashMap<>();

    protected final Map<String, Set<ServiceInstance>> registeredServiceInstances = new ConcurrentHashMap<>();

    protected final RegistryConfig config;

    protected final ScopedPlatform platform;

    protected final ThreadPool threadPool;

    protected final String[] addresses;

    protected AbstractRegistryClient(RegistryConfig config, ScopedPlatform platform, boolean needThreadPool) {
        this.config = AssertUtil.notNull(config, "config");
        this.platform = AssertUtil.notNull(platform, "platform");
        this.threadPool = findThreadPool(config, needThreadPool);
        this.addresses = config.address().split(",");
    }

    @Override
    public Future<Void> register(ServiceInstance instance) {
        String serviceName = instance.serviceName();
        Set<ServiceInstance> serviceInstances = registeredServiceInstances.computeIfAbsent(serviceName, k -> ConcurrentHashMap.newKeySet());
        if (serviceInstances.add(instance)) {
            Registration registration = createRegistration(instance);
            RegisterTask registerTask = new RegisterTask(this, instance, registration);
            return registerTask.execute().onComplete(res -> {
                if (res.succeeded()) {
                    platform.singleComponent(Scheduler.class)
                            .addPeriodic(registerTask, 5, 10, TimeUnit.SECONDS);
                    logger.info("Registered instance '{}' of service '{}' at '{}'",
                            instance.id(), serviceName, config);

                } else {
                    logger.error("Failed to register instance '{}' of service '{}' at '{}'", res.cause(),
                            instance.id(), serviceName, config);
                }
            });
        }
        return Promise.completedVoid();
    }

    @Override
    public Future<List<ServiceInstance>> lookup(String serviceName) {
        return subscribedHealthServices.computeIfAbsent(serviceName, k -> {
            DiscoveredService holder = new DiscoveredService();
            doLookup(serviceName).onComplete(res -> {
                if (res.succeeded()) {
                    List<ServiceInstance> instances = res.result();
                    logger.info("Discovered {} instance(s) for '{}' '{}'", instances.size(), serviceName, config);
                    holder.complete(instances);
                    threadPool.execute(() -> {
                        try {
                            doSubscribe(serviceName);
                            logger.info("Subscribed service(s) for '{}' from '{}'", serviceName, config);
                        } catch (Throwable t) {
                            subscribedHealthServices.remove(serviceName);
                        }
                    });

                } else {
                    logger.error("Failed to discover instance(s) for '{}' from '{}'", res.cause(), serviceName, config);
                    holder.promise().failure(res.cause());
                }
            });
            return holder;
        }).promise();

    }

    @Override
    public Future<Void> deregister(ServiceInstance instance) {
        String serviceName = instance.serviceName();
        return doDeregister(instance).onComplete(res -> {
                    if (res.succeeded()) {
                        logger.info("Deregistered instance '{}' of service '{}' at '{}'",
                                instance.id(), serviceName, config);
                    } else {
                        logger.error("Failed to deregister instance '{}' of service '{}' at '{}'", res.cause(),
                                instance.id(), serviceName, config);
                    }
                }
        );
    }

    @Override
    public ScopedPlatform platform() {
        return platform;
    }

    @Override
    public void close() {
        deregisterServices().onComplete(res -> {
            subscribedHealthServices.clear();
            registeredServiceInstances.clear();
            try {
                doClose();
            } catch (Throwable t) {
                logger.error( "Failed to close registry client connected to '{}'", t,this);
            }
        });
    }

    @Override
    public String toString() {
        return config.toString();
    }

    protected void onServicesUpdated(String serviceName, List<ServiceInstance> instances) {
        subscribedHealthServices.get(serviceName).update(instances);
        logger.trace("Updated {} instance(s) for '{}' from '{}'", instances.size(), serviceName, config);
    }

    protected abstract Registration createRegistration(ServiceInstance instance);

    protected abstract void doSubscribe(String serviceName) throws Throwable;

    protected abstract Future<Void> doDeregister(ServiceInstance instance);

    protected abstract Future<List<ServiceInstance>> doLookup(String serviceName);

    protected abstract void doClose() throws Throwable;

    private Future<Void> deregisterServices() {
        if (registeredServiceInstances.isEmpty()) {
            return Promise.completedVoid();
        }
        List<Future<Void>> futures = new ArrayList<>();
        for (Map.Entry<String, Set<ServiceInstance>> entry : registeredServiceInstances.entrySet()) {
            for (ServiceInstance instance : entry.getValue()) {
                futures.add(deregister(instance));
            }
        }
        return Promise.allOf(futures);
    }

    private ThreadPool findThreadPool(RegistryConfig config, boolean needThreadPool) {
        if (!needThreadPool) return null;
        ThreadPool threadedPool = config.threadPool();
        if (threadedPool != null) return threadedPool;
        String name = config.type() + "-" + REGISTRY;
        ExecutorService executorService = RpcThreadPool.defaultIOExecutor(name);
        return new ThreadPool(name, executorService);
    }

    protected static class DiscoveredService {

        final Promise<List<ServiceInstance>> promise = new Promise<>();

        final AtomicReference<List<ServiceInstance>> instanceRef = new AtomicReference<>(new ArrayList<>());

        void complete(List<ServiceInstance> instances) {
            update(instances);
            promise.success(Collections.unmodifiableList(instanceRef.get()));
        }

        void update(List<ServiceInstance> instances) {
            CollectionUtil.replaceIfMatch(instanceRef.get(), instances, (o, n) -> o.id().equals(n.id()));
        }

        Promise<List<ServiceInstance>> promise() {
            return promise;
        }

    }

}

