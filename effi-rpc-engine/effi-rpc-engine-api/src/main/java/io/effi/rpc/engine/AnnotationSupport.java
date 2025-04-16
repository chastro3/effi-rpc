package io.effi.rpc.engine;

import io.effi.rpc.common.config.Config;
import io.effi.rpc.common.config.DefaultConfigKeys;
import io.effi.rpc.common.config.NodeConfig;
import io.effi.rpc.common.util.Messages;
import io.effi.rpc.common.util.ReflectionUtil;
import io.effi.rpc.common.util.StringUtil;
import io.effi.rpc.contract.annotation.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Utility class for rpc operations.
 */
public final class AnnotationSupport {

    public static AnnotationStyle checkAnnotationStyle(Class<?> targetType, NodeConfig config) {
        AnnotationStyle annotationStyle = AnnotationStyle.getInstance(config);
        AnnotationStyleParser parser = annotationStyle.parser();
        if (parser != null)
            parser.parseType(targetType, config);
        return annotationStyle;
    }

    public static List<Method> filterMethods(Method[] methods) {
        return Arrays.stream(methods)
                .filter(method ->
                        !method.isAnnotationPresent(UnParse.class)
                                && !ReflectionUtil.isObjectMethod(method))
                .collect(Collectors.toList());
    }

    public static AnnotationStyleParser annotationStyleParserForMethod(Config config,
                                                                       AnnotationStyle annotationStyle) {
        String style = config.get(DefaultConfigKeys.ANNOTATION_STYLE);
        if (StringUtil.isBlank(style)) return annotationStyle.parser();
        return Objects.equals(style, annotationStyle.name())
                ? annotationStyle.parser()
                : AnnotationStyle.getInstance(style).parser();
    }

    public static <C extends Config> C fillConfig(EffiRpcCaller caller, C config) {
        if (caller != null && config != null) {
            fillKV(config, DefaultConfigKeys.PATH, caller::path);
            fillKV(config, DefaultConfigKeys.ANNOTATION_STYLE, caller::style);
            fillKV(config, DefaultConfigKeys.PROTOCOL, caller::protocol);
            fillKV(config, DefaultConfigKeys.APPLICATION, caller::application);
            fillKV(config, DefaultConfigKeys.CLIENT_CONFIG, caller::clientConfig);
            fillKV(config, DefaultConfigKeys.ADDRESS, caller::address);
            fillKV(config, DefaultConfigKeys.FILTERS, caller::filters);
            fillKV(config, DefaultConfigKeys.REGISTRIES, caller::registries);
            fillKV(config, DefaultConfigKeys.SERIALIZATION, caller::serialization);
            fillKV(config, DefaultConfigKeys.COMPRESSION, caller::compression);
            fillKV(config, DefaultConfigKeys.MODULE, caller::module);
            fillKV(config, DefaultConfigKeys.LOAD_BALANCE, caller::loadBalance);
            fillKV(config, DefaultConfigKeys.FAULT_TOLERANCE, caller::faultTolerance);
            fillKV(config, DefaultConfigKeys.THREAD_POOL, caller::threadPool);
            fillKV(config, DefaultConfigKeys.TIMEOUT, caller::timeout);
            fillKV(config, DefaultConfigKeys.RETRIES, caller::retries);
            fillKV(config, DefaultConfigKeys.SERIALIZATION_THRESHOLD, caller::serializationThreshold);
            fillKV(config, DefaultConfigKeys.DESERIALIZATION_THRESHOLD, caller::deserializationThreshold);
        }
        return config;
    }

    public static <C extends Config> C fillConfig(EffiRpcClient client, C config) {
        if (client != null && config != null) {
            fillKV(config, DefaultConfigKeys.PROXY, client::proxy);
            fillKV(config, DefaultConfigKeys.PATH, client::path);
            fillKV(config, DefaultConfigKeys.ANNOTATION_STYLE, client::style);
            fillKV(config, DefaultConfigKeys.PROTOCOL, client::protocol);
            fillKV(config, DefaultConfigKeys.APPLICATION, client::application);
            fillKV(config, DefaultConfigKeys.CLIENT_CONFIG, client::clientConfig);
            fillKV(config, DefaultConfigKeys.ADDRESS, client::address);
            fillKV(config, DefaultConfigKeys.FILTERS, client::filters);
            fillKV(config, DefaultConfigKeys.REGISTRIES, client::registries);
            fillKV(config, DefaultConfigKeys.SERIALIZATION, client::serialization);
            fillKV(config, DefaultConfigKeys.COMPRESSION, client::compression);
            fillKV(config, DefaultConfigKeys.MODULE, client::module);
            fillKV(config, DefaultConfigKeys.LOAD_BALANCE, client::loadBalance);
            fillKV(config, DefaultConfigKeys.FAULT_TOLERANCE, client::faultTolerance);
            fillKV(config, DefaultConfigKeys.THREAD_POOL, client::threadPool);
            fillKV(config, DefaultConfigKeys.TIMEOUT, client::timeout);
            fillKV(config, DefaultConfigKeys.RETRIES, client::retries);
            fillKV(config, DefaultConfigKeys.SERIALIZATION_THRESHOLD, client::serializationThreshold);
            fillKV(config, DefaultConfigKeys.DESERIALIZATION_THRESHOLD, client::deserializationThreshold);
        }
        return config;
    }

    public static <C extends Config> C fillConfig(EffiRpcCallee callee, C config) {
        if (callee != null && config != null) {
            fillKV(config, DefaultConfigKeys.PATH, callee::path);
            fillKV(config, DefaultConfigKeys.ANNOTATION_STYLE, callee::style);
            fillKV(config, DefaultConfigKeys.PROTOCOL, callee::protocol);
            fillKV(config, DefaultConfigKeys.EXCLUDED_PORT, callee::excludedPort);
            fillKV(config, DefaultConfigKeys.MODULES, callee::modules);
            fillKV(config, DefaultConfigKeys.FILTERS, callee::filters);
            fillKV(config, DefaultConfigKeys.CALLEE_DESC, callee::desc);
            fillKV(config, DefaultConfigKeys.SERIALIZATION, callee::serialization);
            fillKV(config, DefaultConfigKeys.COMPRESSION, callee::compression);
            fillKV(config, DefaultConfigKeys.THREAD_POOL, callee::threadPool);
            fillKV(config, DefaultConfigKeys.SERIALIZATION_THRESHOLD, callee::serializationThreshold);
            fillKV(config, DefaultConfigKeys.DESERIALIZATION_THRESHOLD, callee::deserializationThreshold);
        }
        return config;
    }

    public static <C extends Config> C fillConfig(EffiRpcService service, C config) {
        if (service != null && config != null) {
            fillKV(config, DefaultConfigKeys.PATH, service::path);
            fillKV(config, DefaultConfigKeys.ANNOTATION_STYLE, service::style);
            fillKV(config, DefaultConfigKeys.PROTOCOL, service::protocol);
            fillKV(config, DefaultConfigKeys.EXCLUDED_PORT, service::excludedPort);
            fillKV(config, DefaultConfigKeys.MODULES, service::modules);
            fillKV(config, DefaultConfigKeys.FILTERS, service::filters);
            fillKV(config, DefaultConfigKeys.CALLEE_DESC, service::desc);
            fillKV(config, DefaultConfigKeys.SERIALIZATION, service::serialization);
            fillKV(config, DefaultConfigKeys.COMPRESSION, service::compression);
            fillKV(config, DefaultConfigKeys.THREAD_POOL, service::threadPool);
            fillKV(config, DefaultConfigKeys.SERIALIZATION_THRESHOLD, service::serializationThreshold);
            fillKV(config, DefaultConfigKeys.DESERIALIZATION_THRESHOLD, service::deserializationThreshold);
        }
        return config;
    }

    private static <T> void fillKV(Config config, DefaultConfigKeys key, Supplier<T> supplier) {
        T value = supplier.get();
        switch (value) {
            case null -> {
            }
            case String strValue -> {
                if (StringUtil.isNotBlank(strValue)) {
                    config.set(key.key(), strValue);
                }
            }
            case Integer intValue -> {
                if (intValue > 0) {
                    config.set(key.key(), String.valueOf(intValue));
                }
            }
            case Long longValue -> {
                if (longValue > 0) {
                    config.set(key.key(), String.valueOf(longValue));
                }
            }
            case Double doubleValue -> {
                if (doubleValue > 0) {
                    config.set(key.key(), String.valueOf(doubleValue));
                }
            }
            case String[] strArray -> {
                if (strArray.length > 0) {
                    config.set(key.key(), String.join(",", strArray));
                }
            }
            case int[] intArray -> {
                if (intArray.length > 0) {
                    config.set(key.key(), Arrays.stream(intArray)
                            .mapToObj(String::valueOf)
                            .collect(Collectors.joining(",")));
                }
            }
            case long[] longArray -> {
                if (longArray.length > 0) {
                    config.set(key.key(), Arrays.stream(longArray)
                            .mapToObj(String::valueOf)
                            .collect(Collectors.joining(",")));
                }
            }
            case double[] doubleArray -> {
                if (doubleArray.length > 0) {
                    config.set(key.key(), Arrays.stream(doubleArray)
                            .mapToObj(String::valueOf)
                            .collect(Collectors.joining(",")));
                }
            }
            default -> throw new IllegalArgumentException(Messages.unSupport(key.key(), value.getClass()));
        }

    }
}

