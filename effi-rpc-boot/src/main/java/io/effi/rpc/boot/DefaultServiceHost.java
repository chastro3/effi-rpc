package io.effi.rpc.boot;

import io.effi.rpc.base.ServiceHost;
import io.effi.rpc.boot.builder.ServiceHostBuilder;
import io.effi.rpc.component.EffiRpcApplication;
import io.effi.rpc.component.EffiRpcPlatform;
import io.effi.rpc.config.Config;
import io.effi.rpc.config.URL;
import io.effi.rpc.config.URLType;
import io.effi.rpc.config.registry.RegistryConfig;
import io.effi.rpc.config.transport.ServerConfig;
import io.effi.rpc.constant.Tags;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.registry.RegistryFactory;
import io.effi.rpc.registry.RegistryService;
import io.effi.rpc.transport.Protocol;
import io.effi.rpc.transport.TransportSupport;
import io.effi.rpc.transport.endpoint.Server;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.ObjectUtil;
import io.effi.rpc.util.collection.LazyList;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Provide the default implementation of the {@link ServiceHost} that exports the server configuration
 * and registers it with registries.
 */
public class DefaultServiceHost extends EffiRpcPlatform.Holder implements ServiceHost {

    private static final Logger logger = LoggerFactory.getLogger(DefaultServiceHost.class);

    private volatile CompletableFuture<Void> startFuture;

    protected final List<RegistryConfig> registryConfigs = new LazyList<>(ArrayList::new);

    private final ServerConfig serverConfig;

    private final InetSocketAddress exportedAddress;

    private final URL exportedUrl;

    private volatile Server server;

    DefaultServiceHost(ServerConfig serverConfig, InetSocketAddress exportedAddress, EffiRpcPlatform platform, Config config) {
        super(platform);
        this.serverConfig = AssertUtil.notNull(serverConfig, "server config");
        this.exportedAddress = AssertUtil.notNull(exportedAddress, "exported address");
        this.exportedUrl = URL.builder()
                .type(URLType.SERVER)
                .protocol(serverConfig.protocol())
                .address(exportedAddress)
                .params(config.items())
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public CompletableFuture<Void> start() {
        if (startFuture == null) {
            synchronized (this) {
                if (startFuture == null) {
                    CompletableFuture<Void>[] registerFutures = doRegister();
                    // todo 这里会很慢,
                    //  因为每次创建server都会创建eventLoopGroup如果创建的eventLoop比较多的话会很慢
                    this.server = createServer();
                    CompletableFuture<Void> serverFuture = server().bind()
                            .thenRun(() -> {
                                logger.info("Opened ({}) server on port {} with config: {}",
                                        exportedUrl.protocol(), exportedUrl.port(), server.url());
                            });
                    startFuture = CompletableFuture.allOf(serverFuture, CompletableFuture.allOf(registerFutures))
                            .exceptionally(e -> {
                                logger.error("Failed to start service host: '{}'", e, this);
                                return null;
                            });
                }
            }
        }
        return startFuture;
    }

    @Override
    public DefaultServiceHost registerAt(RegistryConfig... registryConfigs) {
        if (CollectionUtil.isNotEmpty(registryConfigs)) {
            CollectionUtil.addUnique(this.registryConfigs, registryConfigs);
        }
        return this;
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

    public Server server() {
        return server;
    }

    protected Server createServer() {
        Protocol protocol = TransportSupport.getProtocol(exportedUrl.protocol());
        return protocol.transporter().getServer(serverConfig, exportedAddress, platform);
    }

    protected CompletableFuture<Void>[] doRegister() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (EffiRpcApplication application : platform.applications()) {
            Collection<RegistryConfig> activeRegistryConfigs = application.listOf(RegistryConfig.class, (name, item) -> item.hasTags(Tags.PROVIDER, Tags.FORCE_ACTIVE));
            Collection<RegistryConfig> registryConfigs = CollectionUtil.merge(activeRegistryConfigs, this.registryConfigs);
            if (CollectionUtil.isEmpty(registryConfigs)) {
                logger.warn("No available registry config(s)");
            } else {
                for (RegistryConfig registryConfig : registryConfigs) {
                    RegistryFactory registryFactory = application.getExtension(RegistryFactory.class, registryConfig.type());
                    RegistryService registryService = registryFactory.getService(registryConfig);
                    CompletableFuture<Void> future = registryService.register(application.name(), this);
                    if (future != null) futures.add(future);
                }
            }
        }
        return futures.isEmpty() ? ObjectUtil.emptyFutureArray() : futures.toArray(ObjectUtil.emptyFutureArray());

    }

    @Override
    public URL url() {
        return exportedUrl;
    }

    @Override
    public String toString() {
        return exportedUrl.toString();
    }

    @Override
    public int hashCode() {
        return comparedKey(this).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ServiceHost other)) return false;
        return compareTo(other) == 0;
    }

    @Override
    public void close() {
        if (server != null && server.isActive()) {
            server.close();
        }
    }

    @Override
    public boolean isActive() {
        return server != null && server.isActive();
    }

    @Override
    public int compareTo(@NotNull ServiceHost o) {
        return Objects.equals(comparedKey(this), comparedKey(o)) ? 0 : -1;
    }

    private String comparedKey(ServiceHost host) {
        return host.url().authority();
    }

    /**
     * Builds {@link DefaultServiceHost} instance.
     */
    public static class Builder extends ServiceHostBuilder<DefaultServiceHost, Builder> {

        @Override
        public DefaultServiceHost build() {
            DefaultServiceHost serviceHost = new DefaultServiceHost(serverConfig, exportedAddress, platform, config);
            return serviceHost.registerAt(registryConfigs);
        }
    }
}

