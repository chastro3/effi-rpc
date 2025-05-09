package io.effi.rpc.engine;

import io.effi.rpc.config.DefaultConfigKeys;
import io.effi.rpc.config.URL;
import io.effi.rpc.config.URLType;
import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.ServerExporter;
import io.effi.rpc.contract.config.RegistryConfig;
import io.effi.rpc.contract.config.ServerConfig;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.contract.repository.CalleeRepository;
import io.effi.rpc.engine.builder.ServerExportBuilder;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.registry.RegistryFactory;
import io.effi.rpc.registry.RegistryService;
import io.effi.rpc.spi.ExtensionLoader;
import io.effi.rpc.transport.Protocol;
import io.effi.rpc.transport.TransportSupport;
import io.effi.rpc.transport.endpoint.Server;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.ObjectUtil;
import io.effi.rpc.util.collection.LazyList;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of the {@link ServerExporter} that exports the server configuration
 * and registers it with registries.
 */
public class DefaultServerExporter implements ServerExporter {

    private static final Logger logger = LoggerFactory.getLogger(DefaultServerExporter.class);

    protected final EffiRpcModule module;

    protected final CalleeRepository calleeManager;

    protected final List<RegistryConfig> registryConfigs = new LazyList<>(ArrayList::new);

    private final ServerConfig serverConfig;

    private final InetSocketAddress exportedAddress;

    private final URL exportedUrl;

    private volatile Server server;

    DefaultServerExporter(URL exportedUrl, InetSocketAddress exportedAddress, ServerConfig serverConfig, EffiRpcModule module) {
        this.exportedUrl = exportedUrl;
        this.serverConfig = serverConfig;
        this.exportedAddress = exportedAddress;
        this.module = module;
        this.calleeManager = new CalleeRepository(module);
        module.serverExporterRepository().register(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void export() {
        if (server == null) {
            synchronized (this) {
                if (server == null) {
                    doRegister();
                    server = openServer();
                    logger.info("Opened {} on port {}, bound to config: {}",
                            ObjectUtil.simpleClassName(server),
                            server.port(),
                            server.url()
                    );
                }
            }
        }
    }

    @Override
    public ServerExporter callee(Callee<?>... callees) {
        if (CollectionUtil.isNotEmpty(callees)) {
            for (Callee<?> ce : callees) {
                if (exportedUrl.protocol().equals(ce.protocol())) {
                    calleeManager.register(ce);
                }
            }
        }
        return this;
    }

    @Override
    public ServerExporter registry(RegistryConfig... registryConfigs) {
        if (CollectionUtil.isNotEmpty(registryConfigs)) {
            CollectionUtil.addUnique(this.registryConfigs, registryConfigs);
        }
        return this;
    }

    @Override
    public CalleeRepository calleeRepository() {
        return calleeManager;
    }

    @Override
    public List<RegistryConfig> registries() {
        return registryConfigs;
    }

    @Override
    public ServerConfig serverConfig() {
        return serverConfig;
    }

    @Override
    public InetSocketAddress exportedAddress() {
        return exportedAddress;
    }

    @Override
    public EffiRpcModule module() {
        return module;
    }

    @Override
    public String repositoryKey() {
        return exportedUrl.authority();
    }

    public Server server() {
        return server;
    }

    protected Server openServer() {
        Protocol protocol = TransportSupport.getProtocol(exportedUrl.protocol());
        return protocol.openServer(serverConfig, exportedAddress, module);
    }

    protected void doRegister() {
        List<RegistryConfig> registryConfigs = new ArrayList<>(this.registryConfigs);
        List<RegistryConfig> sharedRegistryConfigs = module.registryConfigRepository().sharedComponents();
        for (RegistryConfig sharedRegistryConfig : sharedRegistryConfigs) {
            CollectionUtil.addUnique(registryConfigs, sharedRegistryConfig);
        }
        if (CollectionUtil.isEmpty(registryConfigs)) {
            logger.warn("No available registry config(s)");
        } else {
            for (RegistryConfig registryConfig : registryConfigs) {
                URL registryConfigUrl = registryConfig.url().replicate();
                registryConfigUrl.addParam(DefaultConfigKeys.APPLICATION.key(), module.application().name());
                RegistryFactory registryFactory = ExtensionLoader.loadExtension(RegistryFactory.class, registryConfigUrl.protocol());
                RegistryService registryService = registryFactory.getService(module.application(), registryConfigUrl);
                registryService.register(exportedUrl);
            }
        }
    }

    @Override
    public URL url() {
        return exportedUrl;
    }

    @Override
    public String toString() {
        return exportedUrl.toString();
    }

    /**
     * Builds {@link DefaultServerExporter} instances.
     */
    public static class Builder extends ServerExportBuilder<DefaultServerExporter, Builder> {

        @Override
        public DefaultServerExporter build() {
            AssertUtil.notNull(serverConfig, "server");
            AssertUtil.notNull(exportedAddress, "exported address");
            AssertUtil.notNull(module, "module");
            URL exportedUrl = URL.builder()
                    .type(URLType.SERVER)
                    .protocol(serverConfig.protocol())
                    .address(exportedAddress)
                    .params(config().items())
                    .build();
            exportedUrl.addParam(DefaultConfigKeys.APPLICATION.key(), module.application().name());
            exportedUrl.addParam(DefaultConfigKeys.MODULE.key(), module.name());
            return new DefaultServerExporter(exportedUrl, exportedAddress, serverConfig, module);
        }
    }
}

