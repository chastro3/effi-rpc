package io.effi.rpc.engine;

import io.effi.rpc.common.constant.DefaultConfigKeys;
import io.effi.rpc.common.spi.ExtensionLoader;
import io.effi.rpc.common.url.URL;
import io.effi.rpc.common.url.URLType;
import io.effi.rpc.common.util.AssertUtil;
import io.effi.rpc.common.util.CollectionUtil;
import io.effi.rpc.common.util.ObjectUtil;
import io.effi.rpc.common.util.collection.LazyList;
import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.config.RegistryConfig;
import io.effi.rpc.contract.config.ServerConfig;
import io.effi.rpc.contract.manager.CalleeManager;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.contract.module.ServerExporter;
import io.effi.rpc.engine.builder.ServerExportBuilder;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.registry.RegistryFactory;
import io.effi.rpc.registry.RegistryService;
import io.effi.rpc.transport.Protocol;
import io.effi.rpc.transport.TransportSupport;
import io.effi.rpc.transport.endpoint.Server;

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

    protected final CalleeManager calleeManager;

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
        this.calleeManager = new CalleeManager(module);
        module.serverExporterManager().register(this);
    }

    /**
     * Builder method for creating instances of {@link DefaultServerExporter}.
     */
    public static DefaultServerExporterBuilder builder() {
        return new DefaultServerExporterBuilder();
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
    public ServerExporter callee(Callee<?>... callee) {
        if (CollectionUtil.isNotEmpty(callee)) {
            for (Callee<?> ce : callee) {
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
    public CalleeManager calleeManager() {
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
    public String managerKey() {
        return exportedUrl.authority();
    }

    public Server server() {
        return server;
    }

    protected Server openServer() {
        Protocol protocol = TransportSupport.getProtocol(exportedUrl.protocol());
        URL serverUrl = URL.builder()
                .type(URLType.SERVER)
                .protocol(exportedUrl.protocol())
                .address(exportedAddress)
                .params(serverConfig.config().properties())
                .build();
        return protocol.openServer(serverUrl, module);
    }

    protected void doRegister() {
        List<RegistryConfig> registryConfigs = new ArrayList<>(this.registryConfigs);
        List<RegistryConfig> sharedRegistryConfigs = module.registryConfigManager().sharedValues();
        for (RegistryConfig sharedRegistryConfig : sharedRegistryConfigs) {
            CollectionUtil.addUnique(registryConfigs, sharedRegistryConfig);
        }
        if (CollectionUtil.isEmpty(registryConfigs)) {
            logger.warn("No available RegistryConfig(s)");
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

    /**
     * Builder class for constructing {@link DefaultServerExporter} instances.
     */
    public static class DefaultServerExporterBuilder extends ServerExportBuilder<DefaultServerExporter, DefaultServerExporterBuilder> {

        @Override
        public DefaultServerExporter build() {
            AssertUtil.notNull(serverConfig, "server");
            AssertUtil.notNull(exportedAddress, "exported address");
            AssertUtil.notNull(module, "module");
            URL exportedUrl = URL.builder()
                    .type(URLType.SERVER)
                    .protocol(serverConfig.protocol())
                    .address(exportedAddress)
                    .params(config().properties())
                    .build();
            exportedUrl.addParam(DefaultConfigKeys.APPLICATION.key(), module.application().name());
            exportedUrl.addParam(DefaultConfigKeys.MODULE.key(), module.name());
            return new DefaultServerExporter(exportedUrl, exportedAddress, serverConfig, module);
        }
    }
}

