package io.effi.rpc.contract.module;

import io.effi.rpc.common.util.CollectionUtil;
import io.effi.rpc.common.util.StringUtil;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.ThreadPool;
import io.effi.rpc.contract.config.ClientConfig;
import io.effi.rpc.contract.config.RegistryConfig;
import io.effi.rpc.contract.filter.Filter;
import io.effi.rpc.contract.manager.*;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import org.intellij.lang.annotations.Language;

/**
 * Manage all entry configurations.
 */
public class EffiRpcModule extends Node {

    private static final Logger logger = LoggerFactory.getLogger(EffiRpcModule.class);

    private CallerManager callerManager;

    private ClientConfigManager clientConfigManager;

    private RegistryConfigManager registryConfigManager;

    private ServerExporterManager serverExporterManager;

    private FilterManager filterManager;

    private RouterConfigManager routerConfigManager;

    private ThreadPoolManager threadPoolManager;

    private MonitorManager monitorManager;

    EffiRpcModule(String name, EffRpcApplication application) {
        initialize(name, application, ModuleConfiguration.class);
    }

    @Override
    protected boolean doInit() {
        this.callerManager = new CallerManager(this);
        this.clientConfigManager = new ClientConfigManager(this);
        this.registryConfigManager = new RegistryConfigManager(this);
        this.serverExporterManager = new ServerExporterManager(this);
        this.filterManager = new FilterManager(this);
        this.routerConfigManager = new RouterConfigManager(this);
        this.threadPoolManager = new ThreadPoolManager(this);
        this.monitorManager = new MonitorManager();
        return super.doInit();
    }

    @Override
    protected boolean doStart() {
        if (StringUtil.isBlank(name)) {
            logger.warn("Module name is blank");
        }
        for (ServerExporter serverExporter : serverExporterManager.components()) {
            serverExporter.export();
        }
        return super.doStart();
    }

    @Override
    protected boolean doStop() {
        logger.info("{} is stopped", name);
        return super.doStop();
    }

    /**
     * Returns the application.
     */
    public EffRpcApplication application() {
        return (EffRpcApplication) parent();
    }

    /**
     * Returns the callerManager.
     */
    public CallerManager callerManager() {
        return callerManager;
    }

    /**
     * Returns the clientConfigManager.
     */
    public ClientConfigManager clientConfigManager() {
        return clientConfigManager;
    }

    /**
     * Returns the registryConfigManager.
     */
    public RegistryConfigManager registryConfigManager() {
        return registryConfigManager;
    }

    /**
     * Returns the serverExporterManager.
     */
    public ServerExporterManager serverExporterManager() {
        return serverExporterManager;
    }

    /**
     * Returns the filterManager.
     */
    public FilterManager filterManager() {
        return filterManager;
    }

    /**
     * Returns the routerConfigManager.
     */
    public RouterConfigManager routerConfigManager() {
        return routerConfigManager;
    }

    /**
     * Returns the threadPoolManager.
     */
    public ThreadPoolManager threadPoolManager() {
        return threadPoolManager;
    }

    /**
     * Returns the monitorManager.
     */
    public MonitorManager monitorManager() {
        return monitorManager;
    }

    /**
     * Registers a router.
     *
     * @param urlRegex
     * @param targetRegex
     * @return
     */
    public EffiRpcModule router(@Language("RegExp") String urlRegex, @Language("RegExp") String targetRegex) {
        routerConfigManager().register(urlRegex, targetRegex);
        return this;
    }

    public EffiRpcModule register(Caller<?>... callers) {
        if (CollectionUtil.isNotEmpty(callers)) {
            for (Caller<?> caller : callers) {
                callerManager().register(caller);
            }
        }
        return this;
    }

    public EffiRpcModule register(ClientConfig... clientConfigs) {
        if (CollectionUtil.isNotEmpty(clientConfigs)) {
            for (ClientConfig clientConfig : clientConfigs) {
                clientConfigManager().register(clientConfig);
            }
        }
        return this;
    }

    public EffiRpcModule register(ServerExporter... serverExporters) {
        if (CollectionUtil.isNotEmpty(serverExporters)) {
            for (ServerExporter serverExporter : serverExporters) {
                serverExporterManager().register(serverExporter);
            }
        }
        return this;
    }

    public EffiRpcModule register(RegistryConfig... registryConfigs) {
        if (CollectionUtil.isNotEmpty(registryConfigs)) {
            for (RegistryConfig registryConfig : registryConfigs) {
                registryConfigManager().register(registryConfig);
            }
        }
        return this;
    }

    public EffiRpcModule register(String name, Filter<?, ?, ?> filter) {
        filterManager().register(name, filter);
        return this;
    }

    public EffiRpcModule register(ThreadPool... threadPools) {
        if (CollectionUtil.isNotEmpty(threadPools)) {
            for (ThreadPool threadPool : threadPools) {
                threadPoolManager().register(threadPool);
            }
        }
        return this;
    }

    public EffiRpcModule registerShared(RegistryConfig... registryConfigs) {
        registryConfigManager().registerShared(registryConfigs);
        return this;
    }

    public EffiRpcModule registerShared(Filter<?, ?, ?>... filters) {
        filterManager().registerShared(filters);
        return this;
    }

    @Override
    public String toString() {
        return "'" + name + "' Module";
    }
}
