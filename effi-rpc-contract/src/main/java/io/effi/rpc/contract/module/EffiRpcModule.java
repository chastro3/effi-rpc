package io.effi.rpc.contract.module;

import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.StringUtil;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.ServerExporter;
import io.effi.rpc.contract.ThreadPool;
import io.effi.rpc.contract.config.ClientConfig;
import io.effi.rpc.contract.config.RegistryConfig;
import io.effi.rpc.contract.filter.Filter;
import io.effi.rpc.contract.repository.*;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import org.intellij.lang.annotations.Language;

/**
 * Manages configurations and components required for RPC.
 */
public class EffiRpcModule extends Node {

    private static final Logger logger = LoggerFactory.getLogger(EffiRpcModule.class);

    private CallerRepository callerRepository;

    private ClientConfigRepository clientConfigRepository;

    private RegistryConfigRepository registryConfigRepository;

    private ServerExporterRepository serverExporterRepository;

    private FilterRepository filterRepository;

    private RouterConfigRepository routerConfigRepository;

    private ThreadPoolRepository threadPoolRepository;

    private MonitorManager monitorManager;

    EffiRpcModule(String name, EffRpcApplication application) {
        initialize(name, application, ModuleConfiguration.class);
    }

    @Override
    protected boolean doInit() {
        this.callerRepository = new CallerRepository(this);
        this.clientConfigRepository = new ClientConfigRepository(this);
        this.registryConfigRepository = new RegistryConfigRepository(this);
        this.serverExporterRepository = new ServerExporterRepository(this);
        this.filterRepository = new FilterRepository(this);
        this.routerConfigRepository = new RouterConfigRepository(this);
        this.threadPoolRepository = new ThreadPoolRepository(this);
        this.monitorManager = new MonitorManager();
        return super.doInit();
    }

    @Override
    protected boolean doStart() {
        if (StringUtil.isBlank(name)) {
            logger.warn("module name is blank");
        }
        for (ServerExporter serverExporter : serverExporterRepository.components()) {
            serverExporter.export();
        }
        return super.doStart();
    }

    @Override
    protected boolean doStop() {
        this.callerRepository.clear();
        this.clientConfigRepository.clear();
        this.registryConfigRepository.clear();
        this.serverExporterRepository.clear();
        this.filterRepository.clear();
        this.routerConfigRepository.clear();
        this.threadPoolRepository.clear();
        logger.info("{} is stopped", name);
        return super.doStop();
    }

    public EffRpcApplication application() {
        return (EffRpcApplication) parent();
    }

    public CallerRepository callerRepository() {
        return callerRepository;
    }

    public ClientConfigRepository clientConfigRepository() {
        return clientConfigRepository;
    }

    public RegistryConfigRepository registryConfigRepository() {
        return registryConfigRepository;
    }

    public ServerExporterRepository serverExporterRepository() {
        return serverExporterRepository;
    }

    public FilterRepository filterRepository() {
        return filterRepository;
    }

    public RouterConfigRepository routerConfigRepository() {
        return routerConfigRepository;
    }

    public ThreadPoolRepository threadPoolRepository() {
        return threadPoolRepository;
    }

    public MonitorManager monitorManager() {
        return monitorManager;
    }

    public EffiRpcModule router(@Language("RegExp") String urlRegex, @Language("RegExp") String targetRegex) {
        routerConfigRepository().register(urlRegex, targetRegex);
        return this;
    }

    public EffiRpcModule register(Caller<?>... callers) {
        if (CollectionUtil.isNotEmpty(callers)) {
            for (Caller<?> caller : callers) {
                callerRepository().register(caller);
            }
        }
        return this;
    }

    public EffiRpcModule register(ClientConfig... clientConfigs) {
        if (CollectionUtil.isNotEmpty(clientConfigs)) {
            for (ClientConfig clientConfig : clientConfigs) {
                clientConfigRepository().register(clientConfig);
            }
        }
        return this;
    }

    public EffiRpcModule register(ServerExporter... serverExporters) {
        if (CollectionUtil.isNotEmpty(serverExporters)) {
            for (ServerExporter serverExporter : serverExporters) {
                serverExporterRepository().register(serverExporter);
            }
        }
        return this;
    }

    public EffiRpcModule register(RegistryConfig... registryConfigs) {
        if (CollectionUtil.isNotEmpty(registryConfigs)) {
            for (RegistryConfig registryConfig : registryConfigs) {
                registryConfigRepository().register(registryConfig);
            }
        }
        return this;
    }

    public EffiRpcModule register(String name, Filter<?, ?, ?> filter) {
        filterRepository().register(name, filter);
        return this;
    }

    public EffiRpcModule register(ThreadPool... threadPools) {
        if (CollectionUtil.isNotEmpty(threadPools)) {
            for (ThreadPool threadPool : threadPools) {
                threadPoolRepository().register(threadPool);
            }
        }
        return this;
    }

    public EffiRpcModule registerShared(RegistryConfig... registryConfigs) {
        registryConfigRepository().registerShared(registryConfigs);
        return this;
    }

    public EffiRpcModule registerShared(Filter<?, ?, ?>... filters) {
        filterRepository().registerShared(filters);
        return this;
    }

    @Override
    public String toString() {
        return "'" + name + "' Module";
    }
}
