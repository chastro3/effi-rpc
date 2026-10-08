package io.effi.rpc.spring.consumer;

import io.effi.rpc.component.serialization.options.CompressionOptions;
import io.effi.rpc.component.serialization.options.SerializationOptions;
import io.effi.rpc.context.options.CallerOptions;
import io.effi.rpc.context.options.FaultToleranceOptions;
import io.effi.rpc.context.options.GovernanceOptions;
import io.effi.rpc.context.options.ThreadPoolOptions;
import io.effi.rpc.option.HierarchicalOptions;
import io.effi.rpc.spring.autoconfigure.EffiRpcProperties;

import java.time.Duration;
import java.util.List;

/**
 * Provides mapping of Spring consumer properties onto RPC hierarchical options.
 */
public final class ConsumerOptionMapper {

    private ConsumerOptionMapper() {
    }

    /**
     * Applies consumer property values to the target options.
     *
     * @param options  target options
     * @param consumer consumer properties
     */
    public static void apply(HierarchicalOptions options, EffiRpcProperties.Consumer consumer) {
        if (consumer == null) {
            return;
        }
        options.addOption(CallerOptions.PROTOCOL, consumer.protocol());
        options.addOption(GovernanceOptions.LOCATOR, consumer.locator());
        options.addOption(CallerOptions.TIMEOUT, millis(consumer.timeout()));
        options.addOption(GovernanceOptions.SERVICE_DISCOVERY_TIMEOUT, millis(consumer.serviceDiscoveryTimeout()));
        options.addOption(FaultToleranceOptions.RETRIES, consumer.retries());
        options.addOption(FaultToleranceOptions.RETRY_BACKOFF, millis(consumer.retryBackoff()));
        options.addOption(FaultToleranceOptions.RETRY_MAX_BACKOFF, millis(consumer.retryMaxBackoff()));
        options.addOption(FaultToleranceOptions.RETRY_JITTER, millis(consumer.retryJitter()));
        options.addOption(FaultToleranceOptions.FAILURE_HANDLER, consumer.failureHandler());
        options.addOption(GovernanceOptions.LOAD_BALANCER, consumer.loadBalancer());
        options.addOption(SerializationOptions.SERIALIZER, consumer.serializer());
        options.addOption(CompressionOptions.COMPRESSOR, consumer.compressor());
        options.addOption(ThreadPoolOptions.THREAD_POOL, consumer.threadPool());
        options.addOption(SerializationOptions.SERIALIZATION_THRESHOLD, consumer.serializationThreshold());
        options.addOption(SerializationOptions.DESERIALIZATION_THRESHOLD, consumer.deserializationThreshold());
        options.addOption(GovernanceOptions.REGISTRY, toArray(consumer.registries()));
    }

    private static Integer millis(Duration duration) {
        return duration == null ? null : Math.toIntExact(duration.toMillis());
    }

    private static String[] toArray(List<String> values) {
        return values == null || values.isEmpty() ? null : values.toArray(String[]::new);
    }
}
