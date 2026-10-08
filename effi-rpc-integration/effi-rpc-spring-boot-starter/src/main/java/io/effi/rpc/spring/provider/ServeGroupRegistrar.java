package io.effi.rpc.spring.provider;

import io.effi.rpc.annotation.rpc.ServeGroup;
import io.effi.rpc.boot.AnnotationSupport;
import io.effi.rpc.boot.InterfaceServantGroup;
import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.serialization.options.CompressionOptions;
import io.effi.rpc.component.serialization.options.SerializationOptions;
import io.effi.rpc.context.options.ServantOptions;
import io.effi.rpc.context.options.ThreadPoolOptions;
import io.effi.rpc.option.HierarchicalOptions;
import io.effi.rpc.spring.autoconfigure.EffiRpcProperties;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.StringUtil;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Handles registration of Spring beans annotated with {@link ServeGroup} as RPC servants.
 * <p>
 * Candidate beans are collected before registration so export order does not depend on
 * bean post-processor execution order.
 */
public final class ServeGroupRegistrar implements BeanPostProcessor, SmartInitializingSingleton {

    private final ObjectProvider<EffiRpcProperties> propertiesProvider;

    private final ObjectProvider<ScopedApplication> applicationProvider;

    private final Map<Class<?>, String> exportedInterfaces = new ConcurrentHashMap<>();

    // Collect candidates until all singleton beans are available to make registration order-independent.
    private final List<ServeGroupCandidate> candidates = new CopyOnWriteArrayList<>();

    private volatile boolean ready;

    public ServeGroupRegistrar(
            ObjectProvider<EffiRpcProperties> propertiesProvider,
            ObjectProvider<ScopedApplication> applicationProvider
    ) {
        this.propertiesProvider = propertiesProvider;
        this.applicationProvider = applicationProvider;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ServeGroupRegistrar) {
            return bean;
        }
        ServeGroup annotation = AnnotatedElementUtils.findMergedAnnotation(AopUtils.getTargetClass(bean), ServeGroup.class);
        if (annotation != null) {
            ServeGroupCandidate candidate = new ServeGroupCandidate(beanName, bean, annotation);
            if (ready) {
                register(candidate);
            } else {
                candidates.add(candidate);
            }
        }
        return bean;
    }

    @Override
    public void afterSingletonsInstantiated() {
        candidates.forEach(this::register);
        candidates.clear();
        ready = true;
    }

    private void register(ServeGroupCandidate candidate) {
        String beanName = candidate.beanName();
        Object bean = candidate.bean();
        ServeGroup annotation = candidate.annotation();
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        EffiRpcProperties properties = propertiesProvider.getIfAvailable(EffiRpcProperties::defaults);
        EffiRpcProperties.Provider provider = properties.provider();
        List<Class<?>> interfaces = resolveInterfaces(targetClass, annotation);
        List<String> protocols = resolveProtocols(annotation, provider);
        ScopedModule module = resolveModule(annotation, provider);
        for (Class<?> interfaceType : interfaces) {
            String previous = exportedInterfaces.putIfAbsent(interfaceType, beanName);
            if (previous != null) {
                throw new IllegalStateException("RPC interface '" + interfaceType.getName()
                        + "' is exported by both '" + previous + "' and '" + beanName + "'");
            }
            createServant(interfaceType, bean, module, protocols, annotation, provider);
        }
    }

    private List<Class<?>> resolveInterfaces(Class<?> targetClass, ServeGroup annotation) {
        Class<?>[] configured = annotation.interfaces();
        if (configured.length > 0) {
            return List.of(configured);
        }
        List<Class<?>> candidates = Arrays.stream(targetClass.getInterfaces())
                .filter(this::isBusinessInterface)
                .toList();
        if (candidates.size() == 1) {
            return candidates;
        }
        throw new IllegalStateException("@ServeGroup on '" + targetClass.getName()
                + "' must declare interfaces when it implements " + candidates.size() + " business interfaces");
    }

    // Framework interfaces are excluded because they are not RPC contracts.
    private boolean isBusinessInterface(Class<?> candidate) {
        String name = candidate.getName();
        return !name.startsWith("java.")
                && !name.startsWith("javax.")
                && !name.startsWith("jakarta.")
                && !name.startsWith("org.springframework.")
                && !name.startsWith("io.effi.rpc.");
    }

    private List<String> resolveProtocols(ServeGroup annotation, EffiRpcProperties.Provider provider) {
        String[] declared = annotation.protocol();
        if (declared.length > 0) {
            return List.of(declared);
        }
        if (provider != null && CollectionUtil.isNotEmpty(provider.protocols())) {
            return provider.protocols();
        }
        throw new IllegalStateException("@ServeGroup must configure serve.protocol or define effi.rpc.provider.protocols");
    }

    private ScopedModule resolveModule(ServeGroup annotation, EffiRpcProperties.Provider provider) {
        String moduleName = StringUtil.isNotBlank(annotation.module())
                ? annotation.module()
                : provider == null ? null : provider.module();
        ScopedApplication application = applicationProvider.getObject();
        if (StringUtil.isNotBlank(moduleName)) {
            ScopedModule module = application.lookupModule(moduleName);
            if (module != null) {
                return module;
            }
            throw new IllegalStateException("RPC module '" + moduleName + "' does not exist. Available modules: "
                    + application.modules().stream().map(ScopedModule::name).toList());
        }
        return application.defaultModule();
    }

    private void createServant(
            Class<?> interfaceType,
            Object bean,
            ScopedModule module,
            List<String> protocols,
            ServeGroup annotation,
            EffiRpcProperties.Provider provider
    ) {
        HierarchicalOptions options = HierarchicalOptions.create();
        AnnotationSupport.apply(annotation, options);
        // Spring provider defaults fill values omitted by the annotation.
        options.addOption(ServantOptions.DECLARED_PROTOCOL, protocols.toArray(String[]::new));
        options.addOption(SerializationOptions.SERIALIZER,
                value(annotation.serializer(), provider == null ? null : provider.serializer()));
        options.addOption(CompressionOptions.COMPRESSOR,
                value(annotation.compressor(), provider == null ? null : provider.compressor()));
        options.addOption(ThreadPoolOptions.THREAD_POOL,
                value(annotation.threadPool(), provider == null ? null : provider.threadPool()));
        @SuppressWarnings("unchecked")
        Class<Object> targetType = (Class<Object>) interfaceType;
        InterfaceServantGroup.builder()
                .targetType(targetType)
                .service(bean)
                .module(module)
                .options(options)
                .build();
    }

    private String value(String primary, String fallback) {
        return StringUtil.isNotBlank(primary) ? primary : fallback;
    }

    private record ServeGroupCandidate(String beanName, Object bean, ServeGroup annotation) {
    }
}
