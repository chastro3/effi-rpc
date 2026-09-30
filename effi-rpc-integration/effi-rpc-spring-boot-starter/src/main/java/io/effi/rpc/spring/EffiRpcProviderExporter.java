package io.effi.rpc.spring;

import io.effi.rpc.boot.InterfaceServantGroup;
import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.serialization.options.CompressionOptions;
import io.effi.rpc.context.options.SerializationOptions;
import io.effi.rpc.context.options.ServantOptions;
import io.effi.rpc.context.options.ThreadPoolOptions;
import io.effi.rpc.option.HierarchicalOptions;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.StringUtil;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registers Spring beans annotated with {@link EffiRpcService} as RPC servants.
 */
public final class EffiRpcProviderExporter implements SmartInitializingSingleton, BeanFactoryAware {

    private final Map<Class<?>, String> exportedInterfaces = new ConcurrentHashMap<>();

    private final List<Object> registrations = new ArrayList<>();

    private BeanFactory beanFactory;

    @Override
    public void afterSingletonsInstantiated() {
        if (!(beanFactory instanceof ListableBeanFactory listableBeanFactory)) {
            return;
        }
        Map<String, Object> beans = listableBeanFactory.getBeansOfType(Object.class);
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            registerIfProvider(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    private void registerIfProvider(String beanName, Object bean) {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        EffiRpcService annotation = AnnotatedElementUtils.findMergedAnnotation(targetClass, EffiRpcService.class);
        if (annotation == null) {
            return;
        }

        EffiRpcProperties properties = beanFactory.getBeanProvider(EffiRpcProperties.class)
                .getIfAvailable(EffiRpcProperties::defaults);
        EffiRpcProperties.ProviderCommon common = properties.provider().common();
        List<Class<?>> interfaces = resolveInterfaces(targetClass, annotation);
        List<String> protocols = resolveProtocols(annotation, common);
        ScopedModule module = resolveModule(annotation, common);

        for (Class<?> interfaceType : interfaces) {
            String previous = exportedInterfaces.putIfAbsent(interfaceType, beanName);
            if (previous != null) {
                throw new IllegalStateException("RPC interface '" + interfaceType.getName()
                        + "' is exported by both '" + previous + "' and '" + beanName + "'");
            }
            registrations.add(createServant(interfaceType, bean, module, protocols, annotation, common));
        }
    }

    private List<Class<?>> resolveInterfaces(Class<?> targetClass, EffiRpcService annotation) {
        Class<?>[] configured = annotation.interfaces();
        if (configured.length > 0) {
            return List.of(configured);
        }
        List<Class<?>> candidates = Arrays.stream(targetClass.getInterfaces())
                .filter(EffiRpcProviderExporter::isBusinessInterface)
                .toList();
        if (candidates.size() == 1) {
            return candidates;
        }
        throw new IllegalStateException("@EffiRpcService on '" + targetClass.getName()
                + "' must declare interfaces when it implements " + candidates.size() + " business interfaces");
    }

    private static boolean isBusinessInterface(Class<?> candidate) {
        String name = candidate.getName();
        return !name.startsWith("java.")
                && !name.startsWith("javax.")
                && !name.startsWith("jakarta.")
                && !name.startsWith("org.springframework.")
                && !name.startsWith("io.effi.rpc.");
    }

    private List<String> resolveProtocols(EffiRpcService annotation, EffiRpcProperties.ProviderCommon common) {
        if (annotation.protocols().length > 0) {
            return List.of(annotation.protocols());
        }
        if (common != null && CollectionUtil.isNotEmpty(common.protocols())) {
            return common.protocols();
        }
        throw new IllegalStateException("@EffiRpcService must configure protocols or define effi.rpc.provider.common.protocols");
    }

    private ScopedModule resolveModule(EffiRpcService annotation, EffiRpcProperties.ProviderCommon common) {
        String moduleName = StringUtil.isNotBlank(annotation.module())
                ? annotation.module()
                : common == null ? null : common.module();
        ScopedApplication application = beanFactory.getBean(ScopedApplication.class);
        if (StringUtil.isNotBlank(moduleName)) {
            ScopedModule module = application.lookupModule(moduleName);
            if (module != null) {
                return module;
            }
        }
        return application.defaultModule();
    }

    private Object createServant(
            Class<?> interfaceType,
            Object bean,
            ScopedModule module,
            List<String> protocols,
            EffiRpcService annotation,
            EffiRpcProperties.ProviderCommon common
    ) {
        HierarchicalOptions options = HierarchicalOptions.create();
        options.addOption(ServantOptions.DECLARED_PROTOCOL, protocols.toArray(String[]::new));
        options.addOption(SerializationOptions.SERIALIZER, value(annotation.serializer(), common == null ? null : common.serializer()));
        options.addOption(CompressionOptions.COMPRESSOR, value(annotation.compression(), common == null ? null : common.compression()));
        options.addOption(ThreadPoolOptions.THREAD_POOL, value(annotation.threadPool(), common == null ? null : common.threadPool()));
        @SuppressWarnings("unchecked")
        Class<Object> targetType = (Class<Object>) interfaceType;
        return InterfaceServantGroup.builder()
                .targetType(targetType)
                .service(bean)
                .module(module)
                .options(options)
                .build();
    }

    private static String value(String primary, String fallback) {
        return StringUtil.isNotBlank(primary) ? primary : fallback;
    }
}
