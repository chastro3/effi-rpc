package io.effi.rpc.spring.support;

import io.effi.rpc.boot.ServerLauncher;
import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.registry.DefaultRegistryConfig;
import io.effi.rpc.component.registry.RegistryConfig;
import io.effi.rpc.component.registry.options.RegistryOptions;
import io.effi.rpc.component.transport.ServerConfig;
import io.effi.rpc.component.transport.options.ServerOptions;
import io.effi.rpc.constant.Tags;
import io.effi.rpc.option.Options;
import io.effi.rpc.protocol.http.h1.Http1Protocol;
import io.effi.rpc.protocol.http.h1.Http1ServerConfig;
import io.effi.rpc.protocol.http.h2.Http2Protocol;
import io.effi.rpc.protocol.http.h2.Http2ServerConfig;
import io.effi.rpc.spring.autoconfigure.EffiRpcProperties;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.NetUtil;
import io.effi.rpc.util.StringUtil;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides infrastructure configuration for the RPC platform and application.
 */
public final class InfrastructureConfigurer {

    private InfrastructureConfigurer() {
    }

    /**
     * Registers configured registries on the platform.
     *
     * @param platform target platform
     * @param properties Spring configuration
     */
    public static void registerRegistries(ScopedPlatform platform, EffiRpcProperties properties) {
        List<String> providerRegistries = properties.provider().registries();
        Set<String> registryNames = new LinkedHashSet<>(properties.registries().keySet());
        for (String providerRegistry : providerRegistries) {
            if (!registryNames.contains(providerRegistry)) {
                throw new IllegalStateException("Provider registry '" + providerRegistry
                        + "' is not defined. Available registries: " + registryNames);
            }
        }
        for (Map.Entry<String, EffiRpcProperties.Registry> entry : properties.registries().entrySet()) {
            String name = entry.getKey();
            EffiRpcProperties.Registry registry = entry.getValue();
            DefaultRegistryConfig config = DefaultRegistryConfig.builder()
                    .id(name)
                    .type(requireText(registry.type(), "type", "registry '" + name + "'"))
                    .address(requireText(registry.address(), "address", "registry '" + name + "'"))
                    .connectTimeout(millis(registry.connectTimeout(), RegistryOptions.CONNECT_TIMEOUT.defaultValue()))
                    .retries(registry.retries() == null ? RegistryOptions.RETRIES.defaultValue() : registry.retries())
                    .heartbeatInterval(millis(registry.heartbeatInterval(), RegistryOptions.HEARTBEAT_INTERVAL.defaultValue()))
                    .build();
            List<String> tags = new ArrayList<>(registry.tags());
            if (providerRegistries.contains(name)) {
                if (!tags.contains(Tags.PROVIDER)) {
                    tags.add(Tags.PROVIDER);
                }
                if (!tags.contains(Tags.FORCE_ACTIVE)) {
                    tags.add(Tags.FORCE_ACTIVE);
                }
            }
            if (CollectionUtil.isNotEmpty(tags)) {
                config.addTags(tags.toArray(String[]::new));
            }
            platform.registry().register(RegistryConfig.class, name, config);
        }
    }

    /**
     * Attaches configured servers to the application.
     *
     * @param application target application
     * @param properties Spring configuration
     */
    public static void attachServers(ScopedApplication application, EffiRpcProperties properties) {
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

    private static ServerConfig createServerConfig(String name, EffiRpcProperties.Server server) {
        String protocol = server.protocol();
        if (StringUtil.isBlank(protocol)) {
            throw new IllegalStateException("Server protocol is required for server '" + name + "'");
        }
        if (server.port() != null && !NetUtil.isValidPort(server.port())) {
            throw new IllegalStateException("Server port is invalid for server '" + name + "': " + server.port());
        }
        if (Http1Protocol.NAME.equals(protocol)) {
            Http1ServerConfig.Builder builder = Http1ServerConfig.builder().id(name);
            configureServer(builder, server);
            return builder.build();
        }
        if (Http2Protocol.NAME.equals(protocol)) {
            Http2ServerConfig.Builder builder = Http2ServerConfig.builder().id(name);
            configureServer(builder, server);
            return builder.build();
        }
        throw new IllegalStateException("Unsupported server protocol '" + protocol + "' for server '" + name + "'");
    }

    private static void configureServer(Options.Supplier builder, EffiRpcProperties.Server server) {
        if (server.acceptorThreads() != null) {
            builder.addOption(ServerOptions.ACCEPTOR_THREADS, server.acceptorThreads());
        }
        if (server.ioThreads() != null) {
            builder.addOption(ServerOptions.IO_THREADS, server.ioThreads());
        }
    }

    private static int millis(Duration duration, int defaultValue) {
        return duration == null ? defaultValue : Math.toIntExact(duration.toMillis());
    }

    private static String requireText(String value, String property, String owner) {
        if (StringUtil.isBlank(value)) {
            throw new IllegalStateException("Property '" + property + "' is required for " + owner);
        }
        return value;
    }
}
