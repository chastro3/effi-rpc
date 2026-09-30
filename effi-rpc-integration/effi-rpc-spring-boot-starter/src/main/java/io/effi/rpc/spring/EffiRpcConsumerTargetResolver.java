package io.effi.rpc.spring;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Resolves a consumer interface to a configured logical target.
 */
final class EffiRpcConsumerTargetResolver {

    private EffiRpcConsumerTargetResolver() {
    }

    static ResolvedTarget resolve(Class<?> consumerType, EffiRpcProperties.Consumer consumer) {
        Map<String, EffiRpcProperties.ConsumerTarget> targets = consumer.targets();
        List<Map.Entry<String, EffiRpcProperties.ConsumerTarget>> matches = targets.entrySet().stream()
                .filter(entry -> entry.getValue().interfaces().contains(consumerType))
                .toList();
        if (matches.size() > 1) {
            throw new IllegalStateException("Consumer interface '" + consumerType.getName()
                    + "' is configured in multiple targets: " + matches.stream().map(Map.Entry::getKey).toList());
        }

        if (matches.size() == 1) {
            Map.Entry<String, EffiRpcProperties.ConsumerTarget> match = matches.get(0);
            return new ResolvedTarget(match.getKey(), match.getValue());
        }
        String targetName = toKebabCase(consumerType.getSimpleName());
        EffiRpcProperties.ConsumerTarget target =
                targets.getOrDefault(targetName, EffiRpcProperties.ConsumerTarget.defaults());
        return new ResolvedTarget(targetName, target);
    }

    private static String toKebabCase(String value) {
        return value.replaceAll("([a-z0-9])([A-Z])", "$1-$2")
                .replaceAll("([A-Z]+)([A-Z][a-z])", "$1-$2")
                .toLowerCase(Locale.ROOT);
    }

    record ResolvedTarget(String name, EffiRpcProperties.ConsumerTarget target) {
    }
}
