package io.effi.rpc.boot;

import io.effi.rpc.annotation.rpc.EffiRpcCallee;
import io.effi.rpc.annotation.rpc.EffiRpcCaller;
import io.effi.rpc.annotation.rpc.EffiRpcClient;
import io.effi.rpc.annotation.rpc.EffiRpcService;
import io.effi.rpc.context.annotation.AnnotationStyle;
import io.effi.rpc.context.annotation.AnnotationStyleParser;
import io.effi.rpc.context.annotation.UnParse;
import io.effi.rpc.config.Config;
import io.effi.rpc.config.ConfigNames;
import io.effi.rpc.config.HierarchicalConfig;
import io.effi.rpc.util.NumberUtil;
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

    public static AnnotationStyle checkAnnotationStyle(Class<?> targetType, HierarchicalConfig config) {
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
        String style = config.get(ConfigNames.ANNOTATION_STYLE);
        if (StringUtil.isBlank(style)) return annotationStyle.parser();
        return Objects.equals(style, annotationStyle.name())
                ? annotationStyle.parser()
                : AnnotationStyle.getInstance(style).parser();
    }

    public static <C extends Config> C fillConfig(EffiRpcCaller caller, C config) {
        if (caller != null && config != null) {
            config.set(ConfigNames.PATH, caller.path());
            config.set(ConfigNames.ANNOTATION_STYLE, caller.style());
            config.set(ConfigNames.PROTOCOL, caller.protocol());
            config.set(ConfigNames.REMOTE_APPLICATION, caller.remoteApplication());
            config.set(ConfigNames.REMOTE_MODULE, caller.remoteModule());
            config.set(ConfigNames.CLIENT_CONFIG, caller.clientConfig());
            config.set(ConfigNames.ADDRESS, caller.address());
            config.set(ConfigNames.INTERCEPTOR, caller.interceptor());
            config.set(ConfigNames.REGISTRY, caller.registry());
            config.set(ConfigNames.SERIALIZATION, caller.serialization());
            config.set(ConfigNames.COMPRESSION, caller.compression());
            config.set(ConfigNames.MODULE, caller.module());
            config.set(ConfigNames.LOAD_BALANCE, caller.loadBalance());
            config.set(ConfigNames.FAILURE_HANDLER, caller.failureHandler());
            config.set(ConfigNames.THREAD_POOL, caller.threadPool());
            config.set(ConfigNames.TIMEOUT, caller.timeout());
            config.set(ConfigNames.RETRIES, caller.retries());
            config.set(ConfigNames.SERIALIZATION_THRESHOLD, caller.serializationThreshold());
            config.set(ConfigNames.DESERIALIZATION_THRESHOLD, caller.deserializationThreshold());
        }
        return config;
    }

    public static <C extends Config> C fillConfig(EffiRpcClient client, C config) {
        if (client != null && config != null) {
            config.set(ConfigNames.PROXY, client.proxy());
            config.set(ConfigNames.PATH, client.path());
            config.set(ConfigNames.ANNOTATION_STYLE, client.style());
            config.set(ConfigNames.PROTOCOL, client.protocol());
            config.set(ConfigNames.REMOTE_APPLICATION, client.remoteApplication());
            config.set(ConfigNames.REMOTE_MODULE, client.remoteModule());
            config.set(ConfigNames.CLIENT_CONFIG, client.clientConfig());
            config.set(ConfigNames.ADDRESS, client.address());
            config.set(ConfigNames.INTERCEPTOR, client.interceptor());
            config.set(ConfigNames.REGISTRY, client.registry());
            config.set(ConfigNames.SERIALIZATION, client.serialization());
            config.set(ConfigNames.COMPRESSION, client.compression());
            config.set(ConfigNames.MODULE, client.module());
            config.set(ConfigNames.LOAD_BALANCE, client.loadBalance());
            config.set(ConfigNames.FAILURE_HANDLER, client.failureHandler());
            config.set(ConfigNames.THREAD_POOL, client.threadPool());
            config.set(ConfigNames.TIMEOUT, client.timeout());
            config.set(ConfigNames.RETRIES, client.retries());
            config.set(ConfigNames.SERIALIZATION_THRESHOLD, client.serializationThreshold());
            config.set(ConfigNames.DESERIALIZATION_THRESHOLD, client.deserializationThreshold());
        }
        return config;
    }

    public static <C extends Config> C fillConfig(EffiRpcCallee callee, C config) {
        if (callee != null && config != null) {
            config.set(ConfigNames.PATH, callee.path());
            config.set(ConfigNames.ANNOTATION_STYLE, callee.style());
            config.set(ConfigNames.SUPPORTED_PROTOCOL, callee.protocol());
            config.set(ConfigNames.EXCLUDED_PORT, NumberUtil.box(callee.excludedPort()));
            config.set(ConfigNames.MODULE, callee.module());
            config.set(ConfigNames.INTERCEPTOR, callee.interceptor());
            config.set(ConfigNames.CALLEE_DESC, callee.desc());
            config.set(ConfigNames.SERIALIZATION, callee.serialization());
            config.set(ConfigNames.COMPRESSION, callee.compression());
            config.set(ConfigNames.CALLEE_THREAD_POOL, callee.threadPool());
            config.set(ConfigNames.SERIALIZATION_THRESHOLD, callee.serializationThreshold());
            config.set(ConfigNames.DESERIALIZATION_THRESHOLD, callee.deserializationThreshold());
        }
        return config;
    }

    public static <C extends Config> C fillConfig(EffiRpcService service, C config) {
        if (service != null && config != null) {
            config.set(ConfigNames.PATH, service.path());
            config.set(ConfigNames.ANNOTATION_STYLE, service.style());
            config.set(ConfigNames.SUPPORTED_PROTOCOL, service.protocol());
            config.set(ConfigNames.EXCLUDED_PORT, NumberUtil.box(service.excludedPort()));
            config.set(ConfigNames.MODULE, service.module());
            config.set(ConfigNames.INTERCEPTOR, service.interceptor());
            config.set(ConfigNames.CALLEE_DESC, service.desc());
            config.set(ConfigNames.SERIALIZATION, service.serialization());
            config.set(ConfigNames.COMPRESSION, service.compression());
            config.set(ConfigNames.CALLEE_THREAD_POOL, service.threadPool());
            config.set(ConfigNames.SERIALIZATION_THRESHOLD, service.serializationThreshold());
            config.set(ConfigNames.DESERIALIZATION_THRESHOLD, service.deserializationThreshold());
        }
        return config;
    }

}

