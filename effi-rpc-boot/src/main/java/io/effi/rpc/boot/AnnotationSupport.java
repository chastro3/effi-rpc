package io.effi.rpc.boot;

import io.effi.rpc.annotation.rpc.Call;
import io.effi.rpc.annotation.rpc.CallGroup;
import io.effi.rpc.annotation.rpc.Serve;
import io.effi.rpc.annotation.rpc.ServeGroup;
import io.effi.rpc.option.HierarchicalOptions;
import io.effi.rpc.option.Options;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.CallerGroup;
import io.effi.rpc.context.Peer;
import io.effi.rpc.context.Servant;
import io.effi.rpc.component.serialization.options.CompressionOptions;
import io.effi.rpc.context.annotation.AnnotationStyle;
import io.effi.rpc.context.annotation.AnnotationStyleResolver;
import io.effi.rpc.context.annotation.UnParse;
import io.effi.rpc.util.NumberUtil;
import io.effi.rpc.util.ReflectionUtil;
import io.effi.rpc.util.StringUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import io.effi.rpc.context.options.CallerOptions;
import io.effi.rpc.context.options.PeerOptions;
import io.effi.rpc.context.options.SerializationOptions;
import io.effi.rpc.context.options.ServantOptions;
import io.effi.rpc.context.options.FaultToleranceOptions;
import io.effi.rpc.context.options.GovernanceOptions;
import io.effi.rpc.context.options.InterceptorOptions;
import io.effi.rpc.context.options.ThreadPoolOptions;

/**
 * Utility class for rpc operations.
 */
public final class AnnotationSupport {

    public static AnnotationStyle checkAnnotationStyle(Class<?> targetType, HierarchicalOptions options) {
        AnnotationStyle annotationStyle = AnnotationStyle.getInstance(options);
        AnnotationStyleResolver resolver = annotationStyle.resolver();
        if (resolver != null)
            resolver.resolveType(targetType, options);
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

    public static AnnotationStyleResolver annotationStyleParserForMethod(Options options,
                                                                         AnnotationStyle annotationStyle) {
        String style = options.option(PeerOptions.ANNOTATION_STYLE);
        if (StringUtil.isBlank(style)) return annotationStyle.resolver();
        return Objects.equals(style, annotationStyle.name())
                ? annotationStyle.resolver()
                : AnnotationStyle.getInstance(style).resolver();
    }

    public static <C extends Options> C fillOption(Call call, C options) {
        if (call != null && options != null) {
            options.addOption(PeerOptions.PATH, new String[]{call.path()});
            options.addOption(PeerOptions.ANNOTATION_STYLE, call.style());
            options.addOption(CallerOptions.PROTOCOL, call.protocol());
            options.addOption(CallerOptions.REMOTE_APPLICATION, call.remoteApplication());
            options.addOption(CallerOptions.REMOTE_MODULE, call.remoteModule());
            options.addOption(CallerOptions.CLIENT, call.clientConfig());
            options.addOption(CallerOptions.ENDPOINT, call.endpoint());
            options.addOption(InterceptorOptions.INCLUDE, call.interceptor());
            options.addOption(GovernanceOptions.REGISTRY, call.registry());
            options.addOption(SerializationOptions.SERIALIZER, call.serialization());
            options.addOption(CompressionOptions.COMPRESSOR, call.compression());
            options.addOption(PeerOptions.ASSOCIATED_MODULE, call.module());
            options.addOption(GovernanceOptions.LOAD_BALANCER, call.loadBalance());
            options.addOption(FaultToleranceOptions.FAILURE_HANDLER, call.failureHandler());
            options.addOption(ThreadPoolOptions.THREAD_POOL, call.threadPool());
            if (call.timeout() >= 0) {
                options.addOption(CallerOptions.TIMEOUT, call.timeout());
            }
            if (call.serviceDiscoveryTimeout() >= 0) {
                options.addOption(GovernanceOptions.SERVICE_DISCOVERY_TIMEOUT, call.serviceDiscoveryTimeout());
            }
            options.addOption(FaultToleranceOptions.RETRIES, call.retries());
            options.addOption(SerializationOptions.SERIALIZATION_THRESHOLD, call.serializationThreshold());
            options.addOption(SerializationOptions.DESERIALIZATION_THRESHOLD, call.deserializationThreshold());
        }
        return options;
    }

    public static <C extends Options> C fillOption(CallGroup group, C options) {
        if (group != null && options != null) {
            options.addOption(CallerOptions.PROXY, group.proxy());
            fillOption(group.call(), options);
        }
        return options;
    }

    public static <C extends Options> C fillOption(Serve serve, C options) {
        if (serve != null && options != null) {
            options.addOption(PeerOptions.PATH, new String[]{serve.path()});
            options.addOption(PeerOptions.ANNOTATION_STYLE, serve.style());
            options.addOption(ServantOptions.DECLARED_PROTOCOL, serve.protocol());
            options.addOption(ServantOptions.EXCLUDED_PORT, NumberUtil.box(serve.excludedPort()));
            options.addOption(PeerOptions.ASSOCIATED_MODULE, serve.module());
            options.addOption(InterceptorOptions.INCLUDE, serve.interceptor());
            options.addOption(ServantOptions.LABEL, serve.desc());
            options.addOption(SerializationOptions.SERIALIZER, serve.serialization());
            options.addOption(CompressionOptions.COMPRESSOR, serve.compression());
            options.addOption(ThreadPoolOptions.THREAD_POOL, serve.threadPool());
            options.addOption(SerializationOptions.SERIALIZATION_THRESHOLD, serve.serializationThreshold());
            options.addOption(SerializationOptions.DESERIALIZATION_THRESHOLD, serve.deserializationThreshold());
        }
        return options;
    }

    public static <C extends Options> C fillOption(ServeGroup group, C options) {
        if (group != null && options != null) {
            fillOption(group.serve(), options);
        }
        return options;
    }

}

