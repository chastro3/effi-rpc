package io.effi.rpc.registry;

import io.effi.rpc.base.Scheduler;
import io.effi.rpc.base.ServiceHost;
import io.effi.rpc.base.ThreadPool;
import io.effi.rpc.component.EffiRpcModule;
import io.effi.rpc.component.EffiRpcPlatform;
import io.effi.rpc.config.URL;
import io.effi.rpc.config.registry.RegistryConfig;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.executor.RpcThreadFactory;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.ObjectUtil;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Provides an abstract implementation of {@link io.effi.rpc.registry.RegistryService}.
 */
public abstract class AbstractRegistryService implements RegistryService {

    protected static final String REGISTRY = "registry";

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final Map<String, DiscoveredService> subscribedHealthServices = new ConcurrentHashMap<>();

    protected final Map<String, Set<ServiceHost>> registeredServiceHosts = new ConcurrentHashMap<>();

    protected final RegistryConfig config;

    protected AbstractRegistryService(RegistryConfig config) {
        this.config = AssertUtil.notNull(config, "config");
    }

    @Override
    public CompletableFuture<Void> register(String serviceName, ServiceHost serviceHost) {
        Set<ServiceHost> serviceHosts = registeredServiceHosts.computeIfAbsent(serviceName, k -> ConcurrentHashMap.newKeySet());
        if (!serviceHosts.add(serviceHost)) {
            logger.warn("Service '{}' already has a '{}' instance registered at '{}' using address '{}'",
                    serviceName, serviceHost.serverConfig().protocolStack().name(), config.url().authority(), serviceHost.url().address());
            return null;
        }
        RegistrationAction registrationAction = createRegistrationAction(serviceName, serviceHost);
        RegisterTask registerTask = new RegisterTask(config, serviceName, serviceHost, registrationAction);
        return registerTask.execute()
                .thenRun(() -> {
                    serviceHost.platform().lookup(Scheduler.class).addPeriodic(registerTask, 5, 10, TimeUnit.SECONDS);
                    logger.info("Registered instance '{}' of service '{}' at '{}'",
                            serviceHost.id(), serviceName, config.url().authority());
                })
                .exceptionally(e -> {
                    logger.error("Failed to register instance '{}' of service '{}' at '{}'", e,
                            serviceHost.id(), serviceName, config.url().authority());
                    return null;
                });
    }

    @Override
    public CompletableFuture<List<URL>> discover(String serviceName, EffiRpcModule module) {
        return subscribedHealthServices.computeIfAbsent(serviceName, k -> {
            DiscoveredService holder = new DiscoveredService();
            doDiscover(serviceName, module).whenComplete((urls, e) -> {
                if (e != null) {
                    logger.error("Failed to discover service(s) for '{}' from '{}'", e, serviceName, config.url().authority());
                    holder.future().completeExceptionally(e);
                } else {
                    logger.info("Discovered {} service(s) for '{}' '{}'", urls.size(), serviceName, config.url().authority());
                    holder.complete(urls);
                    CompletableFuture.runAsync(() -> {
                        try {
                            doSubscribe(serviceName);
                            logger.info("Subscribed service(s) for '{}' from '{}'", serviceName, config.url().authority());
                        } catch (Throwable t) {
                            subscribedHealthServices.remove(serviceName);
                        }
                    }, getThreadPool(module.platform()).executor());
                }
            });
            return holder;
        }).future();

    }

    @Override
    public CompletableFuture<Void> deregister(String serviceName, ServiceHost serviceHost) {
        return doDeregister(serviceName, serviceHost)
                .whenComplete((v, e) -> {
                    if (e != null) {
                        logger.error("Failed to deregister instance '{}' of service '{}' at '{}'", e,
                                serviceHost.id(), serviceName, config.url().authority());
                    } else {
                        logger.info("Deregistered instance '{}' of service '{}' at '{}'",
                                serviceHost.id(), serviceName, config.url().authority());
                    }
                });

    }

    @Override
    public void close() {
        deregisterServices().whenComplete((v, e) -> {
            subscribedHealthServices.clear();
            registeredServiceHosts.clear();
            try {
                doClose();
            } catch (Throwable t) {
                throw PredefinedErrorCode.CLOSE_RESOURCE.fail(e, "registry service").toCompletionException();
            }
        });
    }

    protected void onServicesUpdated(String serviceName, List<URL> urls) {
        subscribedHealthServices.get(serviceName).update(urls);
        logger.trace("Updated {} service(s) for '{}' from '{}'", urls.size(), serviceName, config.url().authority());
    }

    protected abstract RegistrationAction createRegistrationAction(String serviceName, ServiceHost serviceHost);

    protected abstract void doSubscribe(String serviceName) throws Throwable;

    protected abstract CompletableFuture<Void> doDeregister(String serviceName, ServiceHost serviceHost);

    protected abstract CompletableFuture<List<URL>> doDiscover(String serviceName, EffiRpcModule module);

    protected abstract void doClose() throws Throwable;

    protected ThreadPool getThreadPool(EffiRpcPlatform platform) {
        ThreadPool threadPool = platform.lookup(ThreadPool.class, REGISTRY);
        if (threadPool == null) {
            synchronized (REGISTRY) {
                threadPool = platform.lookup(ThreadPool.class, REGISTRY);
                if (threadPool == null) {
                    ExecutorService executorService = Executors.newFixedThreadPool(
                            // todo 修改
                            5,
                            new RpcThreadFactory(REGISTRY)
                    );
                    threadPool = new ThreadPool(REGISTRY, executorService);
                    platform.register(ThreadPool.class, threadPool);
                }
            }
        }
        return threadPool;
    }

    private CompletableFuture<Void> deregisterServices() {
        if (registeredServiceHosts.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        ArrayList<CompletableFuture<Void>> futures = new ArrayList<>();
        for (Map.Entry<String, Set<ServiceHost>> entry : registeredServiceHosts.entrySet()) {
            String serviceName = entry.getKey();
            for (ServiceHost serviceHost : entry.getValue()) {
                futures.add(deregister(serviceName, serviceHost));
            }
        }
        return CompletableFuture.allOf(futures.toArray(ObjectUtil.emptyFutureArray()));
    }

    protected static class DiscoveredService {
        final CompletableFuture<List<URL>> future = new CompletableFuture<>();

        final AtomicReference<List<URL>> urlsRef = new AtomicReference<>(new ArrayList<>());

        void complete(List<URL> urls) {
            update(urls);
            future.complete(Collections.unmodifiableList(urlsRef.get()));
        }

        void update(List<URL> updated) {
            CollectionUtil.replaceIfMatch(urlsRef.get(), updated, (o, n) -> o.address().equals(n.address()));
        }

        CompletableFuture<List<URL>> future() {
            return future;
        }
    }

}

