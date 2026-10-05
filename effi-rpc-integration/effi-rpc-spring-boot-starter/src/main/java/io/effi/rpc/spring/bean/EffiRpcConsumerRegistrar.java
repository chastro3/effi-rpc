package io.effi.rpc.spring.bean;

import io.effi.rpc.spring.properties.EffiRpcProperties;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Registers RPC consumer proxy bean definitions declared in {@code effi.rpc.consumer.targets}.
 */
public final class EffiRpcConsumerRegistrar
        implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private static final String CONSUMER_SUFFIX = "EffiRpcConsumer";

    private Environment environment;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        // Runs before @ConfigurationProperties beans are bound.
        EffiRpcProperties properties = EffiRpcPropertiesBinder.bind(environment);
        ListableBeanFactory beanFactory = registry instanceof ListableBeanFactory listableBeanFactory
                ? listableBeanFactory
                : null;
        Set<Class<?>> registered = new HashSet<>();
        for (Map.Entry<String, EffiRpcProperties.ConsumerTarget> entry : properties.consumer().targets().entrySet()) {
            for (Class<?> consumerType : entry.getValue().interfaces()) {
                registerConsumer(registry, beanFactory, consumerType, EffiRpcConsumerMode.INTERFACE, registered,
                        "effi.rpc.consumer.targets." + entry.getKey() + ".interfaces");
            }
        }
        if (beanFactory != null) {
            for (Class<?> consumerType : EffiRpcConsumerScanner.scan(beanFactory)) {
                registerConsumer(registry, beanFactory, consumerType, EffiRpcConsumerMode.ANNOTATION, registered,
                        "@CallGroup scan");
            }
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    private static void registerConsumer(
            BeanDefinitionRegistry registry,
            ListableBeanFactory beanFactory,
            Class<?> consumerType,
            EffiRpcConsumerMode mode,
            Set<Class<?>> registered,
            String source
    ) {
        if (!consumerType.isInterface()) {
            throw new IllegalArgumentException(source + " must contain interfaces: " + consumerType.getName());
        }
        if (!registered.add(consumerType)) {
            throw new IllegalStateException("Consumer interface is registered more than once: "
                    + consumerType.getName());
        }
        if (beanFactory != null && beanFactory.getBeanNamesForType(consumerType, false, false).length > 0) {
            return;
        }
        String beanName = consumerType.getName() + CONSUMER_SUFFIX;
        if (registry.containsBeanDefinition(beanName)) {
            return;
        }
        registry.registerBeanDefinition(beanName, BeanDefinitionBuilder
                .genericBeanDefinition(EffiRpcConsumerFactoryBean.class)
                .addConstructorArgValue(consumerType)
                .addConstructorArgValue(mode)
                .setPrimary(true)
                .getBeanDefinition());
    }
}
