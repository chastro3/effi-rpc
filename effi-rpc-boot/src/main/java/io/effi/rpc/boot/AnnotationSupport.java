package io.effi.rpc.boot;

import io.effi.rpc.annotation.rpc.Call;
import io.effi.rpc.annotation.rpc.CallGroup;
import io.effi.rpc.annotation.rpc.Serve;
import io.effi.rpc.annotation.rpc.ServeGroup;
import io.effi.rpc.boot.confiurator.DefaultInterceptorChainConfigurator;
import io.effi.rpc.boot.confiurator.DefaultThreadPoolConfigurator;
import io.effi.rpc.config.HierarchicalOptions;
import io.effi.rpc.config.Options;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.CallerGroup;
import io.effi.rpc.context.Peer;
import io.effi.rpc.context.Servant;
import io.effi.rpc.context.annotation.AnnotationStyle;
import io.effi.rpc.context.annotation.AnnotationStyleResolver;
import io.effi.rpc.context.annotation.UnParse;
import io.effi.rpc.context.support.failure.FailRetry;
import io.effi.rpc.governance.registry.RegistryLocator;
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
        String style = options.option(Peer.ANNOTATION_STYLE);
        if (StringUtil.isBlank(style)) return annotationStyle.resolver();
        return Objects.equals(style, annotationStyle.name())
                ? annotationStyle.resolver()
                : AnnotationStyle.getInstance(style).resolver();
    }

    public static <C extends Options> C fillOption(Call call, C options) {
        if (call != null && options != null) {
            options.addOption(Peer.PATH, call.path());
            options.addOption(Peer.ANNOTATION_STYLE, call.style());
            options.addOption(Caller.PROTOCOL, call.protocol());
            options.addOption(Caller.REMOTE_APPLICATION, call.remoteApplication());
            options.addOption(Caller.REMOTE_MODULE, call.remoteModule());
            options.addOption(Caller.CLIENT, call.clientConfig());
            options.addOption(Caller.ENDPOINT, call.endpoint());
            options.addOption(DefaultInterceptorChainConfigurator.INTERCEPTOR, call.interceptor());
            options.addOption(RegistryLocator.REGISTRY, call.registry());
            options.addOption(Peer.SERIALIZER, call.serialization());
            options.addOption(Peer.COMPRESSOR, call.compression());
            options.addOption(Peer.ASSOCIATED_MODULE, call.module());
            options.addOption(Caller.LOAD_BALANCER, call.loadBalance());
            options.addOption(Caller.FAILURE_HANDLER, call.failureHandler());
            options.addOption(DefaultThreadPoolConfigurator.THREAD_POOL, call.threadPool());
            options.addOption(Caller.TIMEOUT, call.timeout());
            options.addOption(FailRetry.RETRIES, call.retries());
            options.addOption(Peer.SERIALIZATION_THRESHOLD, call.serializationThreshold());
            options.addOption(Peer.DESERIALIZATION_THRESHOLD, call.deserializationThreshold());
        }
        return options;
    }

    public static <C extends Options> C fillOption(CallGroup group, C options) {
        if (group != null && options != null) {
            options.addOption(CallerGroup.PROXY, group.proxy());
            fillOption(group.call(), options);
        }
        return options;
    }

    public static <C extends Options> C fillOption(Serve serve, C options) {
        if (serve != null && options != null) {
            options.addOption(Peer.PATH, serve.path());
            options.addOption(Peer.ANNOTATION_STYLE, serve.style());
            options.addOption(Servant.DECLARED_PROTOCOL, serve.protocol());
            options.addOption(Servant.EXCLUDED_PORT, NumberUtil.box(serve.excludedPort()));
            options.addOption(Peer.ASSOCIATED_MODULE, serve.module());
            options.addOption(DefaultInterceptorChainConfigurator.INTERCEPTOR, serve.interceptor());
            options.addOption(Servant.LABEL, serve.desc());
            options.addOption(Peer.SERIALIZER, serve.serialization());
            options.addOption(Peer.COMPRESSOR, serve.compression());
            options.addOption(DefaultThreadPoolConfigurator.THREAD_POOL, serve.threadPool());
            options.addOption(Peer.SERIALIZATION_THRESHOLD, serve.serializationThreshold());
            options.addOption(Peer.DESERIALIZATION_THRESHOLD, serve.deserializationThreshold());
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

