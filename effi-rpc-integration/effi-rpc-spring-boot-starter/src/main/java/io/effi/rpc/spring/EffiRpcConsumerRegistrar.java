package io.effi.rpc.spring;

import io.effi.rpc.annotation.rpc.CallGroup;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Registers RPC consumer proxy bean definitions declared in {@code effi.rpc.consumer.targets}.
 */
public final class EffiRpcConsumerRegistrar implements BeanDefinitionRegistryPostProcessor, EnvironmentAware, BeanFactoryAware {

    private static final String CONSUMER_SUFFIX = "EffiRpcConsumer";

    private Environment environment;

    private BeanFactory beanFactory;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
        // Runs before @ConfigurationProperties beans are bound.
        EffiRpcProperties properties = Binder.get(environment)
                .bind("effi.rpc", Bindable.of(EffiRpcProperties.class))
                .orElseGet(EffiRpcProperties::defaults);
        Set<Class<?>> registered = new HashSet<>();
        for (Map.Entry<String, EffiRpcProperties.ConsumerTarget> entry : properties.consumer().targets().entrySet()) {
            for (Class<?> consumerType : entry.getValue().interfaces()) {
                registerConsumer(registry, consumerType, EffiRpcConsumerMode.INTERFACE, registered,
                        "effi.rpc.consumer.targets." + entry.getKey() + ".interfaces");
            }
        }
        scanAnnotatedConsumers(registry, registered);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    private void scanAnnotatedConsumers(BeanDefinitionRegistry registry, Set<Class<?>> registered) {
        if (!(beanFactory instanceof ListableBeanFactory listableBeanFactory)) {
            return;
        }
        List<String> packages;
        try {
            packages = AutoConfigurationPackages.get(listableBeanFactory);
        } catch (IllegalStateException ignored) {
            return;
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
        for (String scanPackage : packages) {
            scanner.findCandidateComponents(scanPackage).forEach(candidate -> {
                Class<?> consumerType = ClassUtils.resolveClassName(candidate.getBeanClassName(), classLoader);
                registerConsumer(registry, consumerType, EffiRpcConsumerMode.ANNOTATION, registered, "@CallGroup scan");
            });
        }
    }

    private static void registerConsumer(
            BeanDefinitionRegistry registry,
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
