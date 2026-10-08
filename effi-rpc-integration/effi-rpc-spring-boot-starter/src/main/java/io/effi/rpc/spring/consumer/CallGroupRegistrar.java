package io.effi.rpc.spring.consumer;

import io.effi.rpc.annotation.rpc.CallGroup;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Handles registration of consumer proxy bean definitions for interfaces annotated with {@code @CallGroup}.
 */
public final class CallGroupRegistrar implements ImportBeanDefinitionRegistrar {

    private static final String CONSUMER_SUFFIX = "EffiRpcConsumer";

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        if (!(registry instanceof ListableBeanFactory beanFactory)) {
            return;
        }
        Set<Class<?>> registered = new HashSet<>();
        for (Class<?> consumerType : CallGroupScanner.scan(beanFactory)) {
            registerConsumer(registry, beanFactory, consumerType, registered);
        }
    }

    private void registerConsumer(
            BeanDefinitionRegistry registry,
            ListableBeanFactory beanFactory,
            Class<?> consumerType,
            Set<Class<?>> registered
    ) {
        if (!consumerType.isInterface()) {
            throw new IllegalArgumentException("@CallGroup must be declared on an interface: " + consumerType.getName());
        }
        if (!registered.add(consumerType)) {
            return;
        }
        if (beanFactory.getBeanNamesForType(consumerType, false, false).length > 0) {
            return;
        }
        String beanName = consumerType.getName() + CONSUMER_SUFFIX;
        if (registry.containsBeanDefinition(beanName)) {
            return;
        }
        registry.registerBeanDefinition(beanName, BeanDefinitionBuilder
                .genericBeanDefinition(AnnotationCallGroupFactoryBean.class)
                .addConstructorArgValue(consumerType)
                .setPrimary(true)
                .getBeanDefinition());
    }

    private static final class CallGroupScanner {

        private CallGroupScanner() {
        }

        static List<Class<?>> scan(ListableBeanFactory beanFactory) {
            List<String> packages;
            try {
                packages = AutoConfigurationPackages.get(beanFactory);
            } catch (IllegalStateException ignored) {
                return List.of();
            }
            ClassPathScanningCandidateComponentProvider scanner =
                    new ClassPathScanningCandidateComponentProvider(false) {
                        @Override
                        protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                            return beanDefinition.getMetadata().isInterface();
                        }
                    };
            scanner.addIncludeFilter(new AnnotationTypeFilter(CallGroup.class));
            ClassLoader classLoader = ClassUtils.getDefaultClassLoader();
            List<Class<?>> consumers = new ArrayList<>();
            for (String scanPackage : packages) {
                scanner.findCandidateComponents(scanPackage).forEach(candidate -> consumers.add(
                        ClassUtils.resolveClassName(candidate.getBeanClassName(), classLoader)
                ));
            }
            return consumers;
        }
    }
}
