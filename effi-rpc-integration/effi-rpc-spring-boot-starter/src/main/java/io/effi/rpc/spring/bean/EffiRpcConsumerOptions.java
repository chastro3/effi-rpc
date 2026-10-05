package io.effi.rpc.spring.bean;

import io.effi.rpc.component.serialization.options.CompressionOptions;
import io.effi.rpc.component.serialization.options.SerializationOptions;
import io.effi.rpc.context.options.CallerOptions;
import io.effi.rpc.context.options.FaultToleranceOptions;
import io.effi.rpc.context.options.GovernanceOptions;
import io.effi.rpc.context.options.ThreadPoolOptions;
import io.effi.rpc.option.HierarchicalOptions;
import io.effi.rpc.spring.properties.EffiRpcProperties;
import io.effi.rpc.util.StringUtil;

import java.time.Duration;
import java.util.List;

/**
 * Applies Spring consumer properties to RPC hierarchical options.
 */
final class EffiRpcConsumerOptions {

    private EffiRpcConsumerOptions() {
    }

    static void apply(
            HierarchicalOptions options,
            EffiRpcProperties.Consumer consumer,
            EffiRpcConsumerTargetResolver.ResolvedTarget resolvedTarget
    ) {
        if (consumer != null) {
            applyCommon(options, consumer.common());
        }
        applyCommon(options, resolvedTarget.target().overrides());

        String endpoint = resolvedTarget.target().endpoint();
        options.addOption(CallerOptions.ENDPOINT,
                StringUtil.isBlank(endpoint) ? resolvedTarget.name() : endpoint);
    }

    private static void applyCommon(HierarchicalOptions options, EffiRpcProperties.ConsumerCommon common) {
        if (common == null) {
            return;
        }
        options.addOption(CallerOptions.PROTOCOL, common.protocol());
        options.addOption(GovernanceOptions.LOCATOR, common.locator());
        options.addOption(CallerOptions.TIMEOUT, millis(common.timeout()));
        options.addOption(GovernanceOptions.SERVICE_DISCOVERY_TIMEOUT, millis(common.serviceDiscoveryTimeout()));
        options.addOption(FaultToleranceOptions.RETRIES, common.retries());
        options.addOption(FaultToleranceOptions.RETRY_BACKOFF, millis(common.retryBackoff()));
        options.addOption(FaultToleranceOptions.RETRY_MAX_BACKOFF, millis(common.retryMaxBackoff()));
        options.addOption(FaultToleranceOptions.RETRY_JITTER, millis(common.retryJitter()));
        options.addOption(FaultToleranceOptions.FAILURE_HANDLER, common.failureHandler());
        options.addOption(GovernanceOptions.LOAD_BALANCER, common.loadBalancer());
        options.addOption(SerializationOptions.SERIALIZER, common.serializer());
        options.addOption(CompressionOptions.COMPRESSOR, common.compression());
        options.addOption(ThreadPoolOptions.THREAD_POOL, common.threadPool());
        options.addOption(SerializationOptions.SERIALIZATION_THRESHOLD, common.serializationThreshold());
        options.addOption(SerializationOptions.DESERIALIZATION_THRESHOLD, common.deserializationThreshold());
        options.addOption(GovernanceOptions.REGISTRY, toArray(common.registries()));
    }

    private static Integer millis(Duration duration) {
        return duration == null ? null : Math.toIntExact(duration.toMillis());
    }

    private static String[] toArray(List<String> values) {
        return values == null || values.isEmpty() ? null : values.toArray(String[]::new);
    }
}
