package io.effi.rpc.boot;

import io.effi.rpc.annotation.rpc.EffiRpcCallee;
import io.effi.rpc.annotation.rpc.EffiRpcCaller;
import io.effi.rpc.annotation.rpc.EffiRpcClient;
import io.effi.rpc.annotation.rpc.EffiRpcService;
import io.effi.rpc.base.annotation.AnnotationStyle;
import io.effi.rpc.base.annotation.AnnotationStyleParser;
import io.effi.rpc.base.annotation.UnParse;
import io.effi.rpc.config.Config;
import io.effi.rpc.config.DefaultConfigNames;
import io.effi.rpc.config.NodeConfig;
import io.effi.rpc.util.Messages;
import io.effi.rpc.util.ReflectionUtil;
import io.effi.rpc.util.StringUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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
                                && !ReflectionUtil.isObjectMethod(method)
                                && !Modifier.isStatic(method.getModifiers()))
                .collect(Collectors.toList());
    }

    public static AnnotationStyleParser annotationStyleParserForMethod(Config config,
                                                                       AnnotationStyle annotationStyle) {
        String style = config.get(DefaultConfigNames.ANNOTATION_STYLE);
        if (StringUtil.isBlank(style)) return annotationStyle.parser();
        return Objects.equals(style, annotationStyle.name())
                ? annotationStyle.parser()
                : AnnotationStyle.getInstance(style).parser();
    }

    public static <C extends Config> C fillConfig(EffiRpcCaller caller, C config) {
        if (caller != null && config != null) {
            fillKV(config, DefaultConfigNames.PATH, caller.path());
            fillKV(config, DefaultConfigNames.ANNOTATION_STYLE, caller.style());
            fillKV(config, DefaultConfigNames.PROTOCOL, caller.protocol());
            fillKV(config, DefaultConfigNames.REMOTE_APPLICATION, caller.remoteApplication());
            fillKV(config, DefaultConfigNames.REMOTE_MODULE, caller.remoteModule());
            fillKV(config, DefaultConfigNames.CLIENT_CONFIG, caller.clientConfig());
            fillKV(config, DefaultConfigNames.ADDRESS, caller.address());
            fillKV(config, DefaultConfigNames.INTERCEPTOR, caller.filters());
            fillKV(config, DefaultConfigNames.REGISTRY, caller.registries());
            fillKV(config, DefaultConfigNames.SERIALIZATION, caller.serialization());
            fillKV(config, DefaultConfigNames.COMPRESSION, caller.compression());
            fillKV(config, DefaultConfigNames.MODULE, caller.module());
            fillKV(config, DefaultConfigNames.LOAD_BALANCE, caller.loadBalance());
            fillKV(config, DefaultConfigNames.FAULT_TOLERANCE, caller.faultTolerance());
            fillKV(config, DefaultConfigNames.CALLER_THREAD_POOL, caller.threadPool());
            fillKV(config, DefaultConfigNames.TIMEOUT, caller.timeout());
            fillKV(config, DefaultConfigNames.RETRIES, caller.retries());
            fillKV(config, DefaultConfigNames.SERIALIZATION_THRESHOLD, caller.serializationThreshold());
            fillKV(config, DefaultConfigNames.DESERIALIZATION_THRESHOLD, caller.deserializationThreshold());
        }
        return config;
    }

    private static <T> void fillKV(Config config, DefaultConfigNames key, T value) {
        if (value == null) {
            return;
        }

        if (value instanceof String strValue) {
            if (StringUtil.isNotBlank(strValue)) {
                config.set(key.realName(), strValue);
            }
        } else if (value instanceof Integer intValue) {
            if (intValue > 0) {
                config.set(key.realName(), String.valueOf(intValue));
            }
        } else if (value instanceof Long longValue) {
            if (longValue > 0) {
                config.set(key.realName(), String.valueOf(longValue));
            }
        } else if (value instanceof Double doubleValue) {
            if (doubleValue > 0) {
                config.set(key.realName(), String.valueOf(doubleValue));
            }
        } else if (value instanceof String[] strArray) {
            if (strArray.length > 0) {
                config.set(key.realName(), String.join(",", strArray));
            }
        } else if (value instanceof int[] intArray) {
            if (intArray.length > 0) {
                config.set(key.realName(), Arrays.stream(intArray)
                        .mapToObj(String::valueOf)
                        .collect(Collectors.joining(",")));
            }
        } else if (value instanceof long[] longArray) {
            if (longArray.length > 0) {
                config.set(key.realName(), Arrays.stream(longArray)
                        .mapToObj(String::valueOf)
                        .collect(Collectors.joining(",")));
            }
        } else if (value instanceof double[] doubleArray) {
            if (doubleArray.length > 0) {
                config.set(key.realName(), Arrays.stream(doubleArray)
                        .mapToObj(String::valueOf)
                        .collect(Collectors.joining(",")));
            }
        } else {
            throw new IllegalArgumentException(Messages.unSupport(key.realName(), value.getClass()));
        }
    }

    public static <C extends Config> C fillConfig(EffiRpcClient client, C config) {
        if (client != null && config != null) {
            fillKV(config, DefaultConfigNames.PROXY, client.proxy());
            fillKV(config, DefaultConfigNames.PATH, client.path());
            fillKV(config, DefaultConfigNames.ANNOTATION_STYLE, client.style());
            fillKV(config, DefaultConfigNames.PROTOCOL, client.protocol());
            fillKV(config, DefaultConfigNames.REMOTE_APPLICATION, client.remoteApplication());
            fillKV(config, DefaultConfigNames.REMOTE_MODULE, client.remoteModule());
            fillKV(config, DefaultConfigNames.CLIENT_CONFIG, client.clientConfig());
            fillKV(config, DefaultConfigNames.ADDRESS, client.address());
            fillKV(config, DefaultConfigNames.INTERCEPTOR, client.filters());
            fillKV(config, DefaultConfigNames.REGISTRY, client.registries());
            fillKV(config, DefaultConfigNames.SERIALIZATION, client.serialization());
            fillKV(config, DefaultConfigNames.COMPRESSION, client.compression());
            fillKV(config, DefaultConfigNames.MODULE, client.module());
            fillKV(config, DefaultConfigNames.LOAD_BALANCE, client.loadBalance());
            fillKV(config, DefaultConfigNames.FAULT_TOLERANCE, client.faultTolerance());
            fillKV(config, DefaultConfigNames.CALLER_THREAD_POOL, client.threadPool());
            fillKV(config, DefaultConfigNames.TIMEOUT, client.timeout());
            fillKV(config, DefaultConfigNames.RETRIES, client.retries());
            fillKV(config, DefaultConfigNames.SERIALIZATION_THRESHOLD, client.serializationThreshold());
            fillKV(config, DefaultConfigNames.DESERIALIZATION_THRESHOLD, client.deserializationThreshold());
        }
        return config;
    }

    public static <C extends Config> C fillConfig(EffiRpcCallee callee, C config) {
        if (callee != null && config != null) {
            fillKV(config, DefaultConfigNames.PATH, callee.path());
            fillKV(config, DefaultConfigNames.ANNOTATION_STYLE, callee.style());
            fillKV(config, DefaultConfigNames.PROTOCOL, callee.protocol());
            fillKV(config, DefaultConfigNames.EXCLUDED_PORT, callee.excludedPort());
            fillKV(config, DefaultConfigNames.MODULE, callee.module());
            fillKV(config, DefaultConfigNames.INTERCEPTOR, callee.filters());
            fillKV(config, DefaultConfigNames.CALLEE_DESC, callee.desc());
            fillKV(config, DefaultConfigNames.SERIALIZATION, callee.serialization());
            fillKV(config, DefaultConfigNames.COMPRESSION, callee.compression());
            fillKV(config, DefaultConfigNames.CALLEE_THREAD_POOL, callee.threadPool());
            fillKV(config, DefaultConfigNames.SERIALIZATION_THRESHOLD, callee.serializationThreshold());
            fillKV(config, DefaultConfigNames.DESERIALIZATION_THRESHOLD, callee.deserializationThreshold());
        }
        return config;
    }

    public static <C extends Config> C fillConfig(EffiRpcService service, C config) {
        if (service != null && config != null) {
            fillKV(config, DefaultConfigNames.PATH, service.path());
            fillKV(config, DefaultConfigNames.ANNOTATION_STYLE, service.style());
            fillKV(config, DefaultConfigNames.PROTOCOL, service.protocol());
            fillKV(config, DefaultConfigNames.EXCLUDED_PORT, service.excludedPort());
            fillKV(config, DefaultConfigNames.MODULE, service.module());
            fillKV(config, DefaultConfigNames.INTERCEPTOR, service.filters());
            fillKV(config, DefaultConfigNames.CALLEE_DESC, service.desc());
            fillKV(config, DefaultConfigNames.SERIALIZATION, service.serialization());
            fillKV(config, DefaultConfigNames.COMPRESSION, service.compression());
            fillKV(config, DefaultConfigNames.CALLEE_THREAD_POOL, service.threadPool());
            fillKV(config, DefaultConfigNames.SERIALIZATION_THRESHOLD, service.serializationThreshold());
            fillKV(config, DefaultConfigNames.DESERIALIZATION_THRESHOLD, service.deserializationThreshold());
        }
        return config;
    }

}

