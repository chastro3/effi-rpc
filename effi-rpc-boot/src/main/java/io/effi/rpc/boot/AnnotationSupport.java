package io.effi.rpc.boot;

import io.effi.rpc.annotation.rpc.Call;
import io.effi.rpc.annotation.rpc.CallGroup;
import io.effi.rpc.annotation.rpc.Serve;
import io.effi.rpc.annotation.rpc.ServeGroup;
import io.effi.rpc.component.serialization.options.CompressionOptions;
import io.effi.rpc.context.annotation.AnnotationStyle;
import io.effi.rpc.context.annotation.AnnotationStyleResolver;
import io.effi.rpc.context.annotation.UnParse;
import io.effi.rpc.context.options.CallerOptions;
import io.effi.rpc.context.options.FaultToleranceOptions;
import io.effi.rpc.context.options.GovernanceOptions;
import io.effi.rpc.context.options.InterceptorOptions;
import io.effi.rpc.context.options.PeerOptions;
import io.effi.rpc.context.options.SerializationOptions;
import io.effi.rpc.context.options.ServantOptions;
import io.effi.rpc.context.options.ThreadPoolOptions;
import io.effi.rpc.option.HierarchicalOptions;
import io.effi.rpc.option.OptionName;
import io.effi.rpc.option.Options;
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
 * Provides annotation-to-option application and style resolution for RPC peers.
 */
public final class AnnotationSupport {

    /**
     * Applies servant-group annotation values to the target options.
     *
     * @param group   servant-group annotation
     * @param options target options
     * @return updated target options
     */
    public static <C extends Options> C apply(ServeGroup group, C options) {
        if (group != null && options != null) {
            addIfNotBlank(options, PeerOptions.ANNOTATION_STYLE, group.annotationStyle());
            addIfNotBlank(options, ServantOptions.DECLARED_PROTOCOL, group.protocol());
            if (group.excludedPort().length > 0) {
                options.addOption(ServantOptions.EXCLUDED_PORT, NumberUtil.box(group.excludedPort()));
            }
            addIfNotBlank(options, PeerOptions.ASSOCIATED_MODULE, group.module());
            addIfNotBlank(options, InterceptorOptions.INCLUDE, group.interceptors());
            addIfNotBlank(options, InterceptorOptions.EXCLUDE, group.excludeInterceptors());
            addIfNotBlank(options, ServantOptions.LABEL, group.label());
            addIfNotBlank(options, SerializationOptions.SERIALIZER, group.serializer());
            addIfNotBlank(options, CompressionOptions.COMPRESSOR, group.compressor());
            addIfNotBlank(options, ThreadPoolOptions.THREAD_POOL, group.threadPool());
            addIfNonNegative(options, SerializationOptions.SERIALIZATION_THRESHOLD, group.serializationThreshold());
            addIfNonNegative(options, SerializationOptions.DESERIALIZATION_THRESHOLD,
                    group.deserializationThreshold());
        }
        return options;
    }

    /**
     * Applies servant annotation values to the target options.
     *
     * @param serve   servant annotation
     * @param options target options
     * @return updated target options
     */
    public static <C extends Options> C apply(Serve serve, C options) {
        if (serve != null && options != null) {
            addIfNotBlank(options, PeerOptions.PATH, new String[]{serve.path()});
            addIfNotBlank(options, PeerOptions.ANNOTATION_STYLE, serve.annotationStyle());
            addIfNotBlank(options, ServantOptions.DECLARED_PROTOCOL, serve.protocol());
            if (serve.excludedPort().length > 0) {
                options.addOption(ServantOptions.EXCLUDED_PORT, NumberUtil.box(serve.excludedPort()));
            }
            addIfNotBlank(options, PeerOptions.ASSOCIATED_MODULE, serve.module());
            addIfNotBlank(options, InterceptorOptions.INCLUDE, serve.interceptors());
            addIfNotBlank(options, InterceptorOptions.EXCLUDE, serve.excludeInterceptors());
            addIfNotBlank(options, ServantOptions.LABEL, serve.label());
            addIfNotBlank(options, SerializationOptions.SERIALIZER, serve.serializer());
            addIfNotBlank(options, CompressionOptions.COMPRESSOR, serve.compressor());
            addIfNotBlank(options, ThreadPoolOptions.THREAD_POOL, serve.threadPool());
            addIfNonNegative(options, SerializationOptions.SERIALIZATION_THRESHOLD, serve.serializationThreshold());
            addIfNonNegative(options, SerializationOptions.DESERIALIZATION_THRESHOLD, serve.deserializationThreshold());
        }
        return options;
    }

    /**
     * Applies caller-group annotation values to the target options.
     *
     * @param group   caller-group annotation
     * @param options target options
     * @return updated target options
     */
    public static <C extends Options> C apply(CallGroup group, C options) {
        if (group != null && options != null) {
            options.addOption(CallerOptions.PROXY, group.proxy());
            addIfNotBlank(options, PeerOptions.ANNOTATION_STYLE, group.annotationStyle());
            addIfNotBlank(options, CallerOptions.PROTOCOL, group.protocol());
            addIfNotBlank(options, GovernanceOptions.LOCATOR, group.locator());
            addIfNotBlank(options, CallerOptions.REMOTE_APPLICATION, group.remoteApplication());
            addIfNotBlank(options, CallerOptions.REMOTE_MODULE, group.remoteModule());
            addIfNotBlank(options, CallerOptions.REMOTE_PLATFORM, group.remotePlatform());
            addIfNotBlank(options, CallerOptions.CLIENT, group.clientConfig());
            addIfNotBlank(options, CallerOptions.ENDPOINT, group.endpoint());
            addIfNotBlank(options, InterceptorOptions.INCLUDE, group.interceptors());
            addIfNotBlank(options, InterceptorOptions.EXCLUDE, group.excludeInterceptors());
            addIfNotBlank(options, GovernanceOptions.REGISTRY, group.registry());
            addIfNotBlank(options, SerializationOptions.SERIALIZER, group.serializer());
            addIfNotBlank(options, CompressionOptions.COMPRESSOR, group.compressor());
            addIfNotBlank(options, PeerOptions.ASSOCIATED_MODULE, group.module());
            addIfNotBlank(options, GovernanceOptions.LOAD_BALANCER, group.loadBalancer());
            addIfNotBlank(options, GovernanceOptions.ROUTER, group.router());
            addIfNotBlank(options, GovernanceOptions.SERVICE_DISCOVERY, group.serviceDiscovery());
            addIfNotBlank(options, GovernanceOptions.GROUP, group.group());
            addIfNotBlank(options, FaultToleranceOptions.FAILURE_HANDLER, group.failureHandler());
            addIfNotBlank(options, ThreadPoolOptions.THREAD_POOL, group.threadPool());
            addIfNonNegative(options, CallerOptions.TIMEOUT, group.timeoutMillis());
            addIfNonNegative(options, GovernanceOptions.SERVICE_DISCOVERY_TIMEOUT,
                    group.serviceDiscoveryTimeoutMillis());
            addIfNonNegative(options, FaultToleranceOptions.RETRIES, group.retries());
            addIfNonNegative(options, FaultToleranceOptions.RETRY_BACKOFF, group.retryBackoffMillis());
            addIfNonNegative(options, FaultToleranceOptions.RETRY_MAX_BACKOFF, group.retryMaxBackoffMillis());
            addIfNonNegative(options, FaultToleranceOptions.RETRY_JITTER, group.retryJitterMillis());
            addIfNonNegative(options, SerializationOptions.SERIALIZATION_THRESHOLD, group.serializationThreshold());
            addIfNonNegative(options, SerializationOptions.DESERIALIZATION_THRESHOLD,
                    group.deserializationThreshold());
        }
        return options;
    }

    /**
     * Applies caller annotation values to the target options.
     *
     * @param call    caller annotation
     * @param options target options
     * @return updated target options
     */
    public static <C extends Options> C apply(Call call, C options) {
        if (call != null && options != null) {
            addIfNotBlank(options, PeerOptions.PATH, new String[]{call.path()});
            addIfNotBlank(options, PeerOptions.ANNOTATION_STYLE, call.annotationStyle());
            addIfNotBlank(options, CallerOptions.PROTOCOL, call.protocol());
            addIfNotBlank(options, GovernanceOptions.LOCATOR, call.locator());
            addIfNotBlank(options, CallerOptions.REMOTE_APPLICATION, call.remoteApplication());
            addIfNotBlank(options, CallerOptions.REMOTE_MODULE, call.remoteModule());
            addIfNotBlank(options, CallerOptions.REMOTE_PLATFORM, call.remotePlatform());
            addIfNotBlank(options, CallerOptions.CLIENT, call.clientConfig());
            addIfNotBlank(options, CallerOptions.ENDPOINT, call.endpoint());
            addIfNotBlank(options, InterceptorOptions.INCLUDE, call.interceptors());
            addIfNotBlank(options, InterceptorOptions.EXCLUDE, call.excludeInterceptors());
            addIfNotBlank(options, GovernanceOptions.REGISTRY, call.registry());
            addIfNotBlank(options, SerializationOptions.SERIALIZER, call.serializer());
            addIfNotBlank(options, CompressionOptions.COMPRESSOR, call.compressor());
            addIfNotBlank(options, PeerOptions.ASSOCIATED_MODULE, call.module());
            addIfNotBlank(options, GovernanceOptions.LOAD_BALANCER, call.loadBalancer());
            addIfNotBlank(options, GovernanceOptions.ROUTER, call.router());
            addIfNotBlank(options, GovernanceOptions.SERVICE_DISCOVERY, call.serviceDiscovery());
            addIfNotBlank(options, GovernanceOptions.GROUP, call.group());
            addIfNotBlank(options, FaultToleranceOptions.FAILURE_HANDLER, call.failureHandler());
            addIfNotBlank(options, ThreadPoolOptions.THREAD_POOL, call.threadPool());
            addIfNonNegative(options, CallerOptions.TIMEOUT, call.timeoutMillis());
            addIfNonNegative(options, GovernanceOptions.SERVICE_DISCOVERY_TIMEOUT,
                    call.serviceDiscoveryTimeoutMillis());
            addIfNonNegative(options, FaultToleranceOptions.RETRIES, call.retries());
            addIfNonNegative(options, FaultToleranceOptions.RETRY_BACKOFF, call.retryBackoffMillis());
            addIfNonNegative(options, FaultToleranceOptions.RETRY_MAX_BACKOFF, call.retryMaxBackoffMillis());
            addIfNonNegative(options, FaultToleranceOptions.RETRY_JITTER, call.retryJitterMillis());
            addIfNonNegative(options, SerializationOptions.SERIALIZATION_THRESHOLD, call.serializationThreshold());
            addIfNonNegative(options, SerializationOptions.DESERIALIZATION_THRESHOLD, call.deserializationThreshold());
        }
        return options;
    }

    /**
     * Resolves the annotation style and applies its type-level options.
     *
     * @param targetType annotated type
     * @param options    target options
     * @return resolved annotation style
     */
    public static AnnotationStyle checkAnnotationStyle(Class<?> targetType, HierarchicalOptions options) {
        AnnotationStyle annotationStyle = AnnotationStyle.getInstance(options);
        AnnotationStyleResolver resolver = annotationStyle.resolver();
        if (resolver != null)
            resolver.resolveType(targetType, options);
        return annotationStyle;
    }

    /**
     * Filters methods eligible for RPC peer registration.
     *
     * @param methods candidate methods
     * @return eligible methods
     */
    public static List<Method> filterMethods(Method[] methods) {
        return Arrays.stream(methods)
                .filter(method ->
                        !method.isAnnotationPresent(UnParse.class)
                                && !ReflectionUtil.isObjectMethod(method)
                                && !Modifier.isStatic(method.getModifiers()))
                .collect(Collectors.toList());
    }

    /**
     * Resolves the annotation style used for one RPC method.
     *
     * @param options         method options
     * @param annotationStyle fallback annotation style
     * @return effective annotation style resolver
     */
    public static AnnotationStyleResolver annotationStyleParserForMethod(Options options,
                                                                         AnnotationStyle annotationStyle) {
        String style = options.option(PeerOptions.ANNOTATION_STYLE);
        if (StringUtil.isBlank(style)) return annotationStyle.resolver();
        return Objects.equals(style, annotationStyle.name())
                ? annotationStyle.resolver()
                : AnnotationStyle.getInstance(style).resolver();
    }

    // Preserve legacy blank defaults only when no explicit option exists in the current scope.
    private static <C extends Options> void addIfNotBlank(C options, OptionName<String> name, String value) {
        if (StringUtil.isNotBlank(value) || !options.items().containsKey(name.name())) {
            options.addOption(name, value);
        }
    }

    private static <C extends Options> void addIfNotBlank(C options, OptionName<String[]> name, String[] value) {
        if (value == null || value.length == 0) {
            return;
        }
        if (hasText(value) || !options.items().containsKey(name.name())) {
            options.addOption(name, value);
        }
    }

    private static boolean hasText(String[] values) {
        for (String value : values) {
            if (StringUtil.isNotBlank(value)) {
                return true;
            }
        }
        return false;
    }

    private static <C extends Options> void addIfNonNegative(C options, OptionName<Integer> name, int value) {
        if (value >= 0) {
            options.addOption(name, value);
        }
    }

    private static <C extends Options> void addIfNonNegative(C options, OptionName<Long> name, long value) {
        if (value >= 0) {
            options.addOption(name, value);
        }
    }
}
