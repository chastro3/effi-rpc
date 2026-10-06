package io.effi.rpc.spring.autoconfigure;

import io.effi.rpc.util.NetUtil;
import io.effi.rpc.util.StringUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Defines Spring Boot configuration for Effi RPC.
 */
@ConfigurationProperties(prefix = "effi.rpc", ignoreUnknownFields = false)
@Validated
public record EffiRpcProperties(
        /**
         * Indicates whether Effi RPC autoconfiguration is active.
         */
        @DefaultValue("true") boolean enabled,
        /**
         * Identifies the RPC application.
         */
        Application application,
        /**
         * Declares servers by logical name.
         */
        @Valid Map<String, Server> servers,
        /**
         * Declares registries by logical name.
         */
        @Valid Map<String, Registry> registries,
        /**
         * Specifies default caller options.
         */
        @Valid Consumer consumer,
        /**
         * Specifies default servant options.
         */
        @Valid Provider provider
) {

    public EffiRpcProperties {
        application = application == null ? new Application(null) : application;
        servers = servers == null ? Map.of() : Map.copyOf(servers);
        registries = registries == null ? Map.of() : Map.copyOf(registries);
        consumer = consumer == null ? Consumer.defaults() : consumer;
        provider = provider == null ? Provider.defaults() : provider;
    }

    /**
     * Returns the default configuration.
     */
    public static EffiRpcProperties defaults() {
        return new EffiRpcProperties(true, null, null, null, null, null);
    }

    public record Application(String name) {
    }

    /**
     * Defines one server binding.
     */
    public record Server(
            /**
             * Specifies the transport protocol extension name.
             */
            @NotBlank String protocol,
            /**
             * Specifies the bound host.
             */
            String host,
            /**
             * Specifies the bound port.
             */
            @Min(0) @Max(65535) Integer port,
            /**
             * Specifies the acceptor thread count.
             */
            Integer acceptorThreads,
            /**
             * Specifies the I/O thread count.
             */
            Integer ioThreads
    ) {

        public Server {
            host = StringUtil.isBlank(host)
                    ? StringUtil.isBlankOrDefault(NetUtil.localHost(), "127.0.0.1")
                    : host;
        }
    }

    /**
     * Defines one registry connection.
     */
    public record Registry(
            /**
             * Specifies the registry extension type.
             */
            @NotBlank String type,
            /**
             * Specifies the registry address.
             */
            @NotBlank String address,
            /**
             * Specifies the registration tags.
             */
            List<String> tags,
            /**
             * Specifies the connection timeout.
             */
            Duration connectTimeout,
            /**
             * Specifies the maximum retry count.
             */
            Integer retries,
            /**
             * Specifies the heartbeat interval.
             */
            Duration heartbeatInterval
    ) {

        public Registry {
            tags = tags == null ? List.of() : List.copyOf(tags);
        }
    }

    /**
     * Defines default caller options.
     */
    public record Consumer(
            /**
             * Specifies the transport protocol extension name.
             */
            String protocol,
            /**
             * Specifies the locator extension name.
             */
            String locator,
            /**
             * Specifies the call timeout.
             */
            Duration timeout,
            /**
             * Specifies the service discovery timeout.
             */
            Duration serviceDiscoveryTimeout,
            /**
             * Specifies the retry count.
             */
            Integer retries,
            /**
             * Specifies the initial retry backoff.
             */
            Duration retryBackoff,
            /**
             * Specifies the maximum retry backoff.
             */
            Duration retryMaxBackoff,
            /**
             * Specifies the retry jitter.
             */
            Duration retryJitter,
            /**
             * Specifies the failure handler extension name.
             */
            String failureHandler,
            /**
             * Specifies the load balancer extension name.
             */
            String loadBalancer,
            /**
             * Specifies the serializer extension name.
             */
            String serializer,
            /**
             * Specifies the compressor extension name.
             */
            String compressor,
            /**
             * Specifies the thread pool extension name.
             */
            String threadPool,
            /**
             * Specifies the registry configuration names.
             */
            List<String> registries,
            /**
             * Specifies the serialization threshold.
             */
            Long serializationThreshold,
            /**
             * Specifies the deserialization threshold.
             */
            Long deserializationThreshold
    ) {

        public Consumer {
            registries = registries == null ? List.of() : List.copyOf(registries);
        }

        /**
         * Returns the default consumer configuration.
         */
        public static Consumer defaults() {
            return new Consumer(null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null);
        }
    }

    /**
     * Defines default servant options.
     */
    public record Provider(
            /**
             * Specifies the transport protocol extension names.
             */
            List<String> protocols,
            /**
             * Specifies the serializer extension name.
             */
            String serializer,
            /**
             * Specifies the compressor extension name.
             */
            String compressor,
            /**
             * Specifies the thread pool extension name.
             */
            String threadPool,
            /**
             * Specifies the associated module name.
             */
            String module,
            /**
             * Specifies the registry configuration names.
             */
            List<String> registries
    ) {

        public Provider {
            protocols = protocols == null ? List.of() : List.copyOf(protocols);
            registries = registries == null ? List.of() : List.copyOf(registries);
        }

        /**
         * Returns the default provider configuration.
         */
        public static Provider defaults() {
            return new Provider(null, null, null, null, null, null);
        }
    }
}
