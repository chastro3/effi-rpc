package io.effi.rpc.spring.consumer;

import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.core.AnnotationCallerGroup;
import io.effi.rpc.option.HierarchicalOptions;
import io.effi.rpc.spring.autoconfigure.EffiRpcProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;

/**
 * Provides annotation-based consumer proxy creation for {@code @CallGroup} interfaces.
 */
public final class AnnotationCallGroupFactoryBean<T> implements FactoryBean<T>, BeanFactoryAware {

    private final Class<T> consumerType;

    private BeanFactory beanFactory;

    public AnnotationCallGroupFactoryBean(Class<T> consumerType) {
        this.consumerType = consumerType;
    }

    @Override
    public T getObject() {
        return createProxy();
    }

    @Override
    public Class<?> getObjectType() {
        return consumerType;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    private T createProxy() {
        EffiRpcProperties properties = beanFactory.getBeanProvider(EffiRpcProperties.class)
                .getIfAvailable(EffiRpcProperties::defaults);
        HierarchicalOptions options = HierarchicalOptions.create();
        ConsumerOptionMapper.apply(options, properties.consumer());
        ScopedModule module = beanFactory.getBean(ScopedModule.class);
        return AnnotationCallerGroup.<T>builder()
                .targetType(consumerType)
                .module(module)
                .options(options)
                .build()
                .proxy();
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
