package io.effi.rpc.contract.module;

import io.effi.rpc.common.constant.Component;
import io.effi.rpc.common.constant.DefaultConfigKeys;
import io.effi.rpc.common.constant.EffiRpcFramework;
import io.effi.rpc.common.event.DisruptorEventDispatcher;
import io.effi.rpc.common.event.Event;
import io.effi.rpc.common.event.EventDispatcher;
import io.effi.rpc.common.executor.RpcThreadPool;
import io.effi.rpc.common.util.ScheduledThreadPool;
import io.effi.rpc.common.util.Scheduler;
import io.effi.rpc.common.spi.ExtensionLoader;
import io.effi.rpc.common.url.Config;
import io.effi.rpc.common.url.URL;
import io.effi.rpc.common.url.URLType;
import io.effi.rpc.common.util.AssertUtil;
import io.effi.rpc.common.util.CollectionUtil;
import io.effi.rpc.common.util.StringUtil;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Centralized management component for handling {@link EffiRpcModule} instances.
 * <p>
 * Provides a singleton for managing modules, configuration, environment setup,
 * event dispatching, and task scheduling. Supports opening, starting, stopping,
 * and interacting with modules, following a configurable and extensible architecture.
 * </p>
 */
public class EffRpcApplication extends Node {

    static final Map<String, EffRpcApplication> APPLICATIONS = new ConcurrentHashMap<>();

    private static final AtomicInteger NUM = new AtomicInteger(0);

    private final Scheduler scheduler;

    private final EventDispatcher eventDispatcher;

    private final Config providerConfig = new Config();

    private final Config consumerConfig = new Config();

    private final EffiRpcModule defaultModule;

    public EffRpcApplication(String name) {
        this(name, new ScheduledThreadPool(), new DisruptorEventDispatcher(null));
    }

    public EffRpcApplication(String name, Scheduler scheduler, EventDispatcher eventDispatcher) {
        AssertUtil.notBlank(name, "application name");
        this.scheduler = AssertUtil.notNull(scheduler, "scheduler");
        this.eventDispatcher = AssertUtil.notNull(eventDispatcher, "eventDispatcher");
        initialize(name, null, ApplicationConfiguration.class);
        this.defaultModule = newModule(Component.DEFAULT);
        APPLICATIONS.putIfAbsent(name, this);
        if (APPLICATIONS.size() == 1) {
            EffiRpcFramework.registerShutdownHook(() -> all().forEach(EffRpcApplication::stop));
        }
    }

    /**
     * Returns all {@link EffRpcApplication} instances.
     */
    public static Collection<EffRpcApplication> all() {
        return APPLICATIONS.values();
    }

    /**
     * Retrieves the application name from the URL.
     *
     * @param url the URL containing the application name
     * @return application name
     */
    public static String getName(URL url) {
        String applicationName = null;
        if (URLType.SERVER.match(url)) {
            applicationName = url.getParam(DefaultConfigKeys.APPLICATION.key());
        } else if (URLType.REQUEST.match(url)) {
            if (StringUtil.isBlank(url.host()) && StringUtil.isNotBlank(url.address())) {
                applicationName = url.address();
            }
        }
        return applicationName;
    }

    /**
     * Acquires an {@link EffRpcApplication} by URL.
     *
     * @param url the URL for locating the application
     * @return the corresponding {@link EffRpcApplication}
     */
    public static EffRpcApplication getInstance(URL url) {
        return getInstance(getName(url));
    }

    /**
     * Acquires an {@link EffRpcApplication} by name.
     *
     * @param name the name of the application
     * @return the corresponding {@link EffRpcApplication}
     */
    public static EffRpcApplication getInstance(String name) {
        return APPLICATIONS.get(name);
    }

    @Override
    protected boolean doStart() {
        modules().forEach(EffiRpcModule::start);
        return super.doStart();
    }

    @Override
    protected boolean doStop() {
        modules().forEach(EffiRpcModule::stop);
        eventDispatcher().close();
        scheduler().close();
        RpcThreadPool.clear();
        ExtensionLoader.clearLoader();
        return super.doStop();
    }

    /**
     * Creates a new {@link EffiRpcModule} with the default configuration.
     *
     * @return a new {@link EffiRpcModule} instance
     */
    public EffiRpcModule newModule() {
        return newModule(null);
    }

    /**
     * Creates a new {@link EffiRpcModule} with a specified name.
     *
     * @param name the module name
     * @return a new {@link EffiRpcModule} instance
     */
    public EffiRpcModule newModule(String name) {
        if (StringUtil.isBlank(name)) {
            name = "module-" + NUM.incrementAndGet();
        }
        EffiRpcModule module = new EffiRpcModule(name, this);
        addChild(module);
        return module;
    }

    public EffiRpcModule getModule(String name) {
        return (EffiRpcModule) children.get(name);
    }

    /**
     * Publishes an event using the event dispatcher.
     *
     * @param event the event to be published
     */
    public void publishEvent(Event<?> event) {
        eventDispatcher().publish(event);
    }

    /**
     * Returns the scheduler.
     */
    public Scheduler scheduler() {
        return scheduler;
    }

    /**
     * Returns the event dispatcher.
     */
    public EventDispatcher eventDispatcher() {
        return eventDispatcher;
    }

    /**
     * Returns the provider configuration.
     */
    public Config providerConfig() {
        return providerConfig;
    }

    /**
     * Returns the consumer configuration.
     */
    public Config consumerConfig() {
        return consumerConfig;
    }

    /**
     * Returns the defaultModule.
     */
    public EffiRpcModule defaultModule() {
        return defaultModule;
    }

    /**
     * Returns the collection of modules in the application.
     */
    public Collection<EffiRpcModule> modules() {
        if (CollectionUtil.isEmpty(children)) {
            return List.of();
        }
        return children.values().stream()
                .map(item -> (EffiRpcModule) item).toList();
    }

    @Override
    public String toString() {
        return "'" + name + "' Application";
    }
}

