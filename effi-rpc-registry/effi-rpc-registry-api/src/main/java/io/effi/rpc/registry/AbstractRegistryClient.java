package io.effi.rpc.registry;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.registry.RegistryConfig;
import io.effi.rpc.component.registry.options.RegistryOptions;
import io.effi.rpc.component.tools.Scheduler;
import io.effi.rpc.component.tools.ThreadPool;
import io.effi.rpc.concurrent.Deadline;
import io.effi.rpc.concurrent.Future;
import io.effi.rpc.concurrent.Futures;
import io.effi.rpc.concurrent.Promise;
import io.effi.rpc.concurrent.Result;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.executor.RpcThreadPool;
import io.effi.rpc.logging.Logger;
import io.effi.rpc.logging.LoggerFactory;
import io.effi.rpc.util.AssertUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Provides the abstract lifecycle and bookkeeping for a registry client.
 */
public abstract class AbstractRegistryClient implements RegistryClient {

    protected static final String REGISTRY = "registry";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final Map<String, DiscoveredService> subscribedHealthServices = new ConcurrentHashMap<>();

    protected final Map<String, Set<ServiceInstance>> registeredServiceInstances = new ConcurrentHashMap<>();

    protected final RegistryConfig config;

    protected final ScopedPlatform platform;

    protected final ThreadPool threadPool;

    protected final Scheduler scheduler;

    protected final String[] addresses;

    private final boolean ownsThreadPool;

    private final AtomicBoolean closed = new AtomicBoolean(false);

    private final Set<ScheduledFuture<?>> scheduledTasks = ConcurrentHashMap.newKeySet();

    protected AbstractRegistryClient(RegistryConfig config, ScopedPlatform platform) {
        this.config = AssertUtil.notNull(config, "config");
        this.platform = AssertUtil.notNull(platform, "platform");
        this.scheduler = AssertUtil.notNull(platform.singleComponent(Scheduler.class), "scheduler");

        ThreadPool configuredThreadPool = config.threadPool();
        if (configuredThreadPool == null) {
            this.threadPool = createThreadPool(config);
            this.ownsThreadPool = true;
        } else {
            this.threadPool = configuredThreadPool;
            this.ownsThreadPool = false;
        }
        this.addresses = config.address().split(",");
    }

    @Override
    public Future<Void> register(ServiceInstance instance) {
        String serviceName = instance.serviceName();
        Set<ServiceInstance> instances = registeredServiceInstances
                .computeIfAbsent(serviceName, key -> ConcurrentHashMap.newKeySet());
        if (!instances.add(instance)) {
            return Futures.completedVoid();
        }

        RegisterTask task = new RegisterTask(this, instance, createRegistration(instance));
        Promise<Void> result = new Promise<>();
        attemptRegistration(instance, serviceName, task, 0, result);
        return result;
    }

    @Override
    public Future<List<ServiceInstance>> lookup(String serviceName) {
        if (closed.get()) {
            return Promise.failed(serviceUnavailable());
        }
        return subscribedHealthServices.computeIfAbsent(serviceName, this::startDiscovery).current();
    }

    @Override
    public Future<Void> deregister(ServiceInstance instance) {
        return doDeregister(instance).onComplete(outcome -> {
            if (outcome.succeeded()) {
                removeRegisteredInstance(instance.serviceName(), instance);
                logger.info("Deregistered instance '{}' of service '{}' at '{}'",
                        instance.id(), instance.serviceName(), config);
            } else {
                logger.error("Failed to deregister instance '{}' of service '{}' at '{}'",
                        outcome.cause(), instance.id(), instance.serviceName(), config);
            }
        });
    }

    @Override
    public ScopedPlatform platform() {
        return platform;
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        cancelScheduledTasks();
        long timeout = Math.max(1, config.option(RegistryOptions.CONNECT_TIMEOUT));
        Futures.withDeadline(deregisterServices(), Deadline.after(timeout, TimeUnit.MILLISECONDS))
                .onComplete(outcome -> release());
    }

    @Override
    public String toString() {
        return config.toString();
    }

    protected void onServicesUpdated(String serviceName, List<ServiceInstance> instances) {
        DiscoveredService service = subscribedHealthServices.get(serviceName);
        if (service != null) {
            service.update(instances);
            logger.trace("Updated {} instance(s) for '{}' from '{}'", instances.size(), serviceName, config);
        }
    }

    protected abstract Registration createRegistration(ServiceInstance instance);

    protected abstract void doSubscribe(String serviceName) throws Throwable;

    protected abstract Future<Void> doDeregister(ServiceInstance instance);

    protected abstract Future<List<ServiceInstance>> doLookup(String serviceName);

    protected abstract void doClose() throws Throwable;

    private void attemptRegistration(
            ServiceInstance instance,
            String serviceName,
            RegisterTask task,
            int attempt,
            Promise<Void> result
    ) {
        task.execute().onComplete(outcome -> {
            if (outcome.succeeded()) {
                if (closed.get()) {
                    removeRegisteredInstance(serviceName, instance);
                    result.failure(serviceUnavailable());
                    return;
                }
                scheduleHeartbeat(task);
                logger.info("Registered instance '{}' of service '{}' at '{}'",
                        instance.id(), serviceName, config);
                result.success(null);
                return;
            }

            int retries = Math.max(0, config.option(RegistryOptions.RETRIES));
            if (attempt < retries) {
                retryRegistration(instance, serviceName, task, attempt, result, outcome.cause());
                return;
            }

            removeRegisteredInstance(serviceName, instance);
            logger.error("Failed to register instance '{}' of service '{}' at '{}' after {} attempt(s)",
                    outcome.cause(), instance.id(), serviceName, config, attempt + 1);
            result.failure(outcome.cause());
        });
    }

    private void retryRegistration(
            ServiceInstance instance,
            String serviceName,
            RegisterTask task,
            int attempt,
            Promise<Void> result,
            EffiRpcException cause
    ) {
        int retries = Math.max(0, config.option(RegistryOptions.RETRIES));
        long retryInterval = Math.max(1, config.option(RegistryOptions.HEARTBEAT_INTERVAL));
        logger.warn("Failed to register instance '{}' of service '{}' at '{}', retrying {}/{}",
                cause, instance.id(), serviceName, config, attempt + 1, retries);

        boolean scheduled = scheduleIfOpen(
                () -> attemptRegistration(instance, serviceName, task, attempt + 1, result),
                retryInterval,
                0
        );
        if (!scheduled) {
            removeRegisteredInstance(serviceName, instance);
            result.failure(serviceUnavailable());
        }
    }

    private void scheduleHeartbeat(RegisterTask task) {
        long interval = Math.max(1, config.option(RegistryOptions.HEARTBEAT_INTERVAL));
        scheduleIfOpen(task, interval, interval);
    }

    private void removeRegisteredInstance(String serviceName, ServiceInstance instance) {
        Set<ServiceInstance> instances = registeredServiceInstances.get(serviceName);
        if (instances == null) {
            return;
        }
        instances.remove(instance);
        if (instances.isEmpty()) {
            registeredServiceInstances.remove(serviceName, instances);
        }
    }

    private DiscoveredService startDiscovery(String serviceName) {
        DiscoveredService service = new DiscoveredService();
        doLookup(serviceName).onComplete(outcome -> completeDiscovery(serviceName, service, outcome));
        return service;
    }

    private void completeDiscovery(
            String serviceName,
            DiscoveredService service,
            Result<List<ServiceInstance>> outcome
    ) {
        if (closed.get()) {
            service.fail(serviceUnavailable());
            return;
        }
        if (outcome.failed()) {
            subscribedHealthServices.remove(serviceName, service);
            service.fail(outcome.cause());
            return;
        }

        List<ServiceInstance> instances = outcome.value();
        logger.info("Discovered {} instance(s) for '{}' '{}'", instances.size(), serviceName, config);
        service.complete(instances);
        threadPool.execute(() -> subscribe(serviceName)).onComplete(subscription -> {
            if (subscription.failed()) {
                subscribedHealthServices.remove(serviceName, service);
                logger.error("Failed to schedule subscription for '{}' from '{}'",
                        subscription.cause(), serviceName, config);
            }
        });
    }

    private void subscribe(String serviceName) {
        try {
            doSubscribe(serviceName);
            logger.info("Subscribed service(s) for '{}' from '{}'", serviceName, config);
        } catch (Throwable cause) {
            subscribedHealthServices.remove(serviceName);
            logger.error("Failed to subscribe service '{}' from '{}'", cause, serviceName, config);
        }
    }

    private Future<Void> deregisterServices() {
        if (registeredServiceInstances.isEmpty()) {
            return Futures.completedVoid();
        }
        List<Future<Void>> futures = new ArrayList<>();
        registeredServiceInstances.values().forEach(instances ->
                instances.forEach(instance -> futures.add(deregister(instance))));
        return Futures.allOf(futures);
    }

    private boolean scheduleIfOpen(Runnable task, long delay, long interval) {
        if (closed.get()) {
            return false;
        }

        ScheduledFuture<?> future;
        try {
            future = interval > 0
                    ? scheduler.addPeriodic(task, delay, interval, TimeUnit.MILLISECONDS)
                    : scheduler.addDisposable(task, delay, TimeUnit.MILLISECONDS);
        } catch (RejectedExecutionException e) {
            return false;
        }

        scheduledTasks.add(future);
        if (!closed.get()) {
            return true;
        }
        future.cancel(false);
        scheduledTasks.remove(future);
        return false;
    }

    private void cancelScheduledTasks() {
        scheduledTasks.forEach(task -> task.cancel(false));
        scheduledTasks.clear();
    }

    private void release() {
        subscribedHealthServices.clear();
        registeredServiceInstances.clear();
        try {
            doClose();
        } catch (Throwable cause) {
            logger.error("Failed to close registry client connected to '{}'", cause, this);
        } finally {
            if (ownsThreadPool) {
                threadPool.executor().shutdown();
            }
        }
    }

    private ThreadPool createThreadPool(RegistryConfig config) {
        String name = config.type() + "-" + REGISTRY;
        ExecutorService executor = RpcThreadPool.defaultIOExecutor(name);
        return new ThreadPool(name, executor);
    }

    private EffiRpcException serviceUnavailable() {
        return PredefinedErrorCode.SERVICE_UNAVAILABLE.fail(config);
    }

    protected static class DiscoveredService {

        private final Promise<List<ServiceInstance>> firstLookup = new Promise<>();

        private volatile List<ServiceInstance> snapshot = List.of();

        private volatile boolean initialized;

        void complete(List<ServiceInstance> instances) {
            update(instances);
            initialized = true;
            firstLookup.success(snapshot);
        }

        void fail(EffiRpcException cause) {
            firstLookup.failure(cause);
        }

        void update(List<ServiceInstance> instances) {
            snapshot = List.copyOf(instances);
        }

        Future<List<ServiceInstance>> current() {
            return initialized ? Promise.completed(snapshot) : firstLookup;
        }
    }
}
