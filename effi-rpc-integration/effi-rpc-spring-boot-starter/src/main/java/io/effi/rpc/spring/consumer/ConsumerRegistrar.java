package io.effi.rpc.spring.consumer;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.HashSet;
import java.util.Set;

/**
 * Handles registration of consumer proxy bean definitions for interfaces annotated with {@code @CallGroup}.
 */
public final class ConsumerRegistrar implements ImportBeanDefinitionRegistrar {

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
}
