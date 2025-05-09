package io.effi.rpc.contract.module;

import io.effi.rpc.config.*;
import io.effi.rpc.constant.Component;
import io.effi.rpc.constant.EffiRpcFramework;
import io.effi.rpc.event.DisruptorEventDispatcher;
import io.effi.rpc.event.Event;
import io.effi.rpc.event.EventDispatcher;
import io.effi.rpc.executor.RpcThreadPool;
import io.effi.rpc.spi.ExtensionLoader;
import io.effi.rpc.util.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages modules, configurations, environments, events, and tasks.
 */
public class EffRpcApplication extends Node {

    static final Map<String, EffRpcApplication> APPLICATIONS = new ConcurrentHashMap<>();

    private static final AtomicInteger NUM = new AtomicInteger(0);

    private final Scheduler scheduler;

    private final EventDispatcher eventDispatcher;

    private final Config providerConfig = new FlatConfig(this);

    private final Config consumerConfig = new FlatConfig(this);

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

    public static Collection<EffRpcApplication> all() {
        return APPLICATIONS.values();
    }

    /**
     * Retrieves the application name from the URL.
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

    public static EffRpcApplication getInstance(URL url) {
        return getInstance(getName(url));
    }

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

    public EffiRpcModule newModule() {
        return newModule(null);
    }

    /**
     * Creates a new {@link EffiRpcModule} with a specified name.
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

    public void publishEvent(Event<?> event) {
        eventDispatcher().publish(event);
    }

    public Scheduler scheduler() {
        return scheduler;
    }

    public EventDispatcher eventDispatcher() {
        return eventDispatcher;
    }

    public Config providerConfig() {
        return providerConfig;
    }

    public Config consumerConfig() {
        return consumerConfig;
    }

    public EffiRpcModule defaultModule() {
        return defaultModule;
    }

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

