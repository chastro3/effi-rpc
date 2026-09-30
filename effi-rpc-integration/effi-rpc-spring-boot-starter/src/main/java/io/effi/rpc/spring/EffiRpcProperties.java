package io.effi.rpc.spring;

import io.effi.rpc.util.NetUtil;
import io.effi.rpc.util.StringUtil;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Immutable Spring Boot configuration for Effi RPC.
 */
@ConfigurationProperties(prefix = "effi.rpc", ignoreUnknownFields = false)
public record EffiRpcProperties(
        Boolean enabled,
        Application application,
        Map<String, Server> servers,
        Map<String, Registry> registries,
        Consumer consumer,
        Provider provider
) {

    public EffiRpcProperties {
        enabled = enabled == null || enabled;
        application = application == null ? new Application(null) : application;
        servers = servers == null ? Map.of() : Map.copyOf(servers);
        registries = registries == null ? Map.of() : Map.copyOf(registries);
        consumer = consumer == null ? Consumer.defaults() : consumer;
        provider = provider == null ? Provider.defaults() : provider;
    }

    public static EffiRpcProperties defaults() {
        return new EffiRpcProperties(null, null, null, null, null, null);
    }

    public record Application(String name) {
    }

    public record Server(
            String protocol,
            String host,
            Integer port,
            Integer acceptorThreads,
            Integer ioThreads
    ) {

        public Server {
            host = StringUtil.isBlank(host)
                    ? StringUtil.isBlankOrDefault(NetUtil.localHost(), "127.0.0.1")
                    : host;
        }
    }

    public record Registry(
            String type,
            String address,
            List<String> tags,
            Duration connectTimeout,
            Integer retries,
            Duration heartbeatInterval
    ) {

        public Registry {
            tags = tags == null ? List.of() : List.copyOf(tags);
        }
    }

    public record Consumer(
            ConsumerCommon common,
            Map<String, ConsumerTarget> targets
    ) {

        public Consumer {
            common = common == null ? ConsumerCommon.defaults() : common;
            targets = targets == null ? Map.of() : Map.copyOf(targets);
        }

        public static Consumer defaults() {
            return new Consumer(null, null);
        }
    }

    public record ConsumerCommon(
            String protocol,
            String locator,
            Duration timeout,
            Duration serviceDiscoveryTimeout,
            Integer retries,
            Duration retryBackoff,
            Duration retryMaxBackoff,
            Duration retryJitter,
            String failureHandler,
            String loadBalancer,
            String serializer,
            String compression,
            String threadPool,
            List<String> registries,
            Long serializationThreshold,
            Long deserializationThreshold
    ) {

        public ConsumerCommon {
            registries = registries == null ? List.of() : List.copyOf(registries);
        }

        public static ConsumerCommon defaults() {
            return new ConsumerCommon(null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null);
        }
    }

    public record ConsumerTarget(
            List<Class<?>> interfaces,
            String endpoint,
            ConsumerCommon overrides
    ) {

        public ConsumerTarget {
            interfaces = interfaces == null ? List.of() : List.copyOf(interfaces);
            overrides = overrides == null ? ConsumerCommon.defaults() : overrides;
        }

        public static ConsumerTarget defaults() {
            return new ConsumerTarget(null, null, null);
        }
    }

    public record Provider(ProviderCommon common) {

        public Provider {
            common = common == null ? ProviderCommon.defaults() : common;
        }

        public static Provider defaults() {
            return new Provider(null);
        }
    }

    public record ProviderCommon(
            List<String> protocols,
            String serializer,
            String compression,
            String threadPool,
            String module,
            List<String> registries
    ) {

        public ProviderCommon {
            protocols = protocols == null ? List.of() : List.copyOf(protocols);
            registries = registries == null ? List.of() : List.copyOf(registries);
        }

        public static ProviderCommon defaults() {
            return new ProviderCommon(null, null, null, null, null, null);
        }
    }
}
