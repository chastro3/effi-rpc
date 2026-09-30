package io.effi.rpc.spring;

import io.effi.rpc.boot.ServerLauncher;
import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.registry.DefaultRegistryConfig;
import io.effi.rpc.component.registry.RegistryConfig;
import io.effi.rpc.component.registry.options.RegistryOptions;
import io.effi.rpc.component.transport.ServerConfig;
import io.effi.rpc.component.transport.options.ServerOptions;
import io.effi.rpc.constant.Tags;
import io.effi.rpc.protocol.http.h1.Http1Protocol;
import io.effi.rpc.protocol.http.h1.Http1ServerConfig;
import io.effi.rpc.protocol.http.h2.Http2Protocol;
import io.effi.rpc.protocol.http.h2.Http2ServerConfig;
import io.effi.rpc.util.CollectionUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.SmartInitializingSingleton;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Registers application-level servers and registries before the RPC lifecycle starts.
 */
public final class EffiRpcInfrastructureInitializer implements SmartInitializingSingleton, BeanFactoryAware {

    private BeanFactory beanFactory;

    @Override
    public void afterSingletonsInstantiated() {
        EffiRpcProperties properties = beanFactory.getBeanProvider(EffiRpcProperties.class)
                .getIfAvailable(EffiRpcProperties::defaults);
        ScopedPlatform platform = beanFactory.getBean(ScopedPlatform.class);
        ScopedApplication application = beanFactory.getBean(ScopedApplication.class);
        registerRegistries(platform, properties);
        attachServers(application, properties);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    private void registerRegistries(ScopedPlatform platform, EffiRpcProperties properties) {
        List<String> providerRegistries = properties.provider().common().registries();
        for (Map.Entry<String, EffiRpcProperties.Registry> entry : properties.registries().entrySet()) {
            String name = entry.getKey();
            EffiRpcProperties.Registry registry = entry.getValue();
            DefaultRegistryConfig config = DefaultRegistryConfig.builder()
                    .id(name)
                    .type(registry.type())
                    .address(registry.address())
                    .connectTimeout(millis(registry.connectTimeout(), RegistryOptions.CONNECT_TIMEOUT.defaultValue()))
                    .retries(registry.retries() == null ? RegistryOptions.RETRIES.defaultValue() : registry.retries())
                    .heartbeatInterval(millis(registry.heartbeatInterval(), RegistryOptions.HEARTBEAT_INTERVAL.defaultValue()))
                    .build();
            List<String> tags = new ArrayList<>(registry.tags());
            if (providerRegistries.contains(name) && !tags.contains(Tags.PROVIDER)) {
                tags.add(Tags.PROVIDER);
            }
            if (CollectionUtil.isNotEmpty(tags)) {
                config.addTags(tags.toArray(String[]::new));
            }
            platform.registry().register(RegistryConfig.class, name, config);
        }
    }

    private void attachServers(ScopedApplication application, EffiRpcProperties properties) {
        for (Map.Entry<String, EffiRpcProperties.Server> entry : properties.servers().entrySet()) {
            String name = entry.getKey();
            EffiRpcProperties.Server server = entry.getValue();
            ServerConfig config = createServerConfig(name, server);
            ServerLauncher.attach(
                    application,
                    config,
                    server.host(),
                    server.port() == null ? 0 : server.port()
            );
        }
    }

    private ServerConfig createServerConfig(String name, EffiRpcProperties.Server server) {
        String protocol = server.protocol();
        if (Http1Protocol.NAME.equals(protocol)) {
            Http1ServerConfig.Builder builder = Http1ServerConfig.builder().id(name);
            if (server.acceptorThreads() != null) {
                builder.addOption(ServerOptions.ACCEPTOR_THREADS, server.acceptorThreads());
            }
            if (server.ioThreads() != null) {
                builder.addOption(ServerOptions.IO_THREADS, server.ioThreads());
            }
            return builder.build();
        }
        if (Http2Protocol.NAME.equals(protocol)) {
            Http2ServerConfig.Builder builder = Http2ServerConfig.builder().id(name);
            if (server.acceptorThreads() != null) {
                builder.addOption(ServerOptions.ACCEPTOR_THREADS, server.acceptorThreads());
            }
            if (server.ioThreads() != null) {
                builder.addOption(ServerOptions.IO_THREADS, server.ioThreads());
            }
            return builder.build();
        }
        throw new IllegalStateException("Unsupported server protocol '" + protocol + "' for server '" + name + "'");
    }

    private static int millis(Duration duration, int defaultValue) {
        return duration == null ? defaultValue : Math.toIntExact(duration.toMillis());
    }
}
