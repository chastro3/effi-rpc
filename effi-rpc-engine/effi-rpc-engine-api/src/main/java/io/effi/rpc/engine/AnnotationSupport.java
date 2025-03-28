package io.effi.rpc.engine;

import io.effi.rpc.common.constant.DefaultConfigKeys;
import io.effi.rpc.common.extension.spi.ExtensionLoader;
import io.effi.rpc.common.url.Config;
import io.effi.rpc.common.util.Messages;
import io.effi.rpc.common.util.StringUtil;
import io.effi.rpc.contract.annotation.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Utility class for rpc operations.
 */
public final class AnnotationSupport {

    public static List<Method> filterMethods(Method[] methods) {
        return Arrays.stream(methods)
                .filter(method ->
                        !method.isAnnotationPresent(UnParse.class)
                                && !Object.class.equals(method.getDeclaringClass()))
                .collect(Collectors.toList());
    }

    public static AnnotationStyleParser annotationStyleParserForType(Config config, String style, Class<?> type) {
        AnnotationStyleParser annotationStyleParser = null;
        if (StringUtil.isNotBlank(style)) {
            annotationStyleParser = ExtensionLoader.loadExtension(AnnotationStyleParser.class, style);
            annotationStyleParser.parseType(type, config);
        }
        return annotationStyleParser;
    }

    public static AnnotationStyleParser annotationStyleParserForMethod(Config config,
                                                                       String typeStyle,
                                                                       AnnotationStyleParser typeAnnotationStyleParser) {
        String style = config.get(DefaultConfigKeys.STYLE);
        if (StringUtil.isBlank(style)) return typeAnnotationStyleParser;
        return Objects.equals(style, typeStyle)
                ? typeAnnotationStyleParser
                : ExtensionLoader.loadExtension(AnnotationStyleParser.class, style);
    }

    public static Config toConfig(EffiRpcCaller caller) {
        return buildConfig(caller, config -> {
            fillConfig(config, DefaultConfigKeys.PATH, caller::path);
            fillConfig(config, DefaultConfigKeys.STYLE, caller::style);
            fillConfig(config, DefaultConfigKeys.PROTOCOL, caller::protocol);
            fillConfig(config, DefaultConfigKeys.APPLICATION, caller::application);
            fillConfig(config, DefaultConfigKeys.CLIENT_CONFIG, caller::clientConfig);
            fillConfig(config, DefaultConfigKeys.ADDRESS, caller::address);
            fillConfig(config, DefaultConfigKeys.FILTERS, caller::filters);
            fillConfig(config, DefaultConfigKeys.REGISTRIES, caller::registries);
            fillConfig(config, DefaultConfigKeys.SERIALIZATION, caller::serialization);
            fillConfig(config, DefaultConfigKeys.COMPRESSION, caller::compression);
            fillConfig(config, DefaultConfigKeys.MODULE, caller::module);
            fillConfig(config, DefaultConfigKeys.LOAD_BALANCE, caller::loadBalance);
            fillConfig(config, DefaultConfigKeys.FAULT_TOLERANCE, caller::faultTolerance);
            fillConfig(config, DefaultConfigKeys.THREAD_POOL, caller::threadPool);
            fillConfig(config, DefaultConfigKeys.TIMEOUT, caller::timeout);
            fillConfig(config, DefaultConfigKeys.RETRIES, caller::retries);
            fillConfig(config, DefaultConfigKeys.SERIALIZATION_THRESHOLD, caller::serializationThreshold);
            fillConfig(config, DefaultConfigKeys.DESERIALIZATION_THRESHOLD, caller::deserializationThreshold);
        });
    }

    public static Config toConfig(EffiRpcClient client) {
        return buildConfig(client, config -> {
            fillConfig(config, DefaultConfigKeys.PROXY, client::proxy);
            fillConfig(config, DefaultConfigKeys.PATH, client::path);
            fillConfig(config, DefaultConfigKeys.STYLE, client::style);
            fillConfig(config, DefaultConfigKeys.PROTOCOL, client::protocol);
            fillConfig(config, DefaultConfigKeys.APPLICATION, client::application);
            fillConfig(config, DefaultConfigKeys.CLIENT_CONFIG, client::clientConfig);
            fillConfig(config, DefaultConfigKeys.ADDRESS, client::address);
            fillConfig(config, DefaultConfigKeys.FILTERS, client::filters);
            fillConfig(config, DefaultConfigKeys.REGISTRIES, client::registries);
            fillConfig(config, DefaultConfigKeys.SERIALIZATION, client::serialization);
            fillConfig(config, DefaultConfigKeys.COMPRESSION, client::compression);
            fillConfig(config, DefaultConfigKeys.MODULE, client::module);
            fillConfig(config, DefaultConfigKeys.LOAD_BALANCE, client::loadBalance);
            fillConfig(config, DefaultConfigKeys.FAULT_TOLERANCE, client::faultTolerance);
            fillConfig(config, DefaultConfigKeys.THREAD_POOL, client::threadPool);
            fillConfig(config, DefaultConfigKeys.TIMEOUT, client::timeout);
            fillConfig(config, DefaultConfigKeys.RETRIES, client::retries);
            fillConfig(config, DefaultConfigKeys.SERIALIZATION_THRESHOLD, client::serializationThreshold);
            fillConfig(config, DefaultConfigKeys.DESERIALIZATION_THRESHOLD, client::deserializationThreshold);
        });
    }

    public static Config toConfig(EffiRpcCallee callee) {
        return buildConfig(callee, config -> {
            fillConfig(config, DefaultConfigKeys.PATH, callee::path);
            fillConfig(config, DefaultConfigKeys.STYLE, callee::style);
            fillConfig(config, DefaultConfigKeys.PROTOCOL, callee::protocol);
            fillConfig(config, DefaultConfigKeys.EXCLUDED_PORT, callee::excludedPort);
            fillConfig(config, DefaultConfigKeys.MODULES, callee::modules);
            fillConfig(config, DefaultConfigKeys.FILTERS, callee::filters);
            fillConfig(config, DefaultConfigKeys.DESC, callee::desc);
            fillConfig(config, DefaultConfigKeys.SERIALIZATION, callee::serialization);
            fillConfig(config, DefaultConfigKeys.COMPRESSION, callee::compression);
            fillConfig(config, DefaultConfigKeys.THREAD_POOL, callee::threadPool);
            fillConfig(config, DefaultConfigKeys.SERIALIZATION_THRESHOLD, callee::serializationThreshold);
            fillConfig(config, DefaultConfigKeys.DESERIALIZATION_THRESHOLD, callee::deserializationThreshold);
        });
    }

    public static Config toConfig(EffiRpcService service) {
        return buildConfig(service, config -> {
            fillConfig(config, DefaultConfigKeys.PATH, service::path);
            fillConfig(config, DefaultConfigKeys.STYLE, service::style);
            fillConfig(config, DefaultConfigKeys.PROTOCOL, service::protocol);
            fillConfig(config, DefaultConfigKeys.EXCLUDED_PORT, service::excludedPort);
            fillConfig(config, DefaultConfigKeys.MODULES, service::modules);
            fillConfig(config, DefaultConfigKeys.FILTERS, service::filters);
            fillConfig(config, DefaultConfigKeys.DESC, service::desc);
            fillConfig(config, DefaultConfigKeys.SERIALIZATION, service::serialization);
            fillConfig(config, DefaultConfigKeys.COMPRESSION, service::compression);
            fillConfig(config, DefaultConfigKeys.THREAD_POOL, service::threadPool);
            fillConfig(config, DefaultConfigKeys.SERIALIZATION_THRESHOLD, service::serializationThreshold);
            fillConfig(config, DefaultConfigKeys.DESERIALIZATION_THRESHOLD, service::deserializationThreshold);
        });
    }

    private static <T> Config buildConfig(T source, Consumer<Config> configFiller) {
        Config config = new Config();
        if (source != null) {
            configFiller.accept(config);
        }
        return config;
    }

    private static <T> void fillConfig(Config config, DefaultConfigKeys key, Supplier<T> supplier) {
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

