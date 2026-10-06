package io.effi.rpc.spring.bean;

import io.effi.rpc.boot.AnnotationCallerGroup;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.option.HierarchicalOptions;
import io.effi.rpc.spring.properties.EffiRpcProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;

/**
 * Builds and caches one annotation-based consumer proxy for an interface.
 */
public final class EffiRpcConsumerFactoryBean<T> implements FactoryBean<T>, BeanFactoryAware {

    private final Class<T> consumerType;

    private BeanFactory beanFactory;

    public EffiRpcConsumerFactoryBean(Class<T> consumerType) {
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

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    private T createProxy() {
        EffiRpcProperties properties = beanFactory.getBeanProvider(EffiRpcProperties.class)
                .getIfAvailable(EffiRpcProperties::defaults);
        HierarchicalOptions options = HierarchicalOptions.create();
        EffiRpcConsumerOptions.apply(options, properties.consumer());
        ScopedModule module = beanFactory.getBean(ScopedModule.class);
        return AnnotationCallerGroup.<T>builder()
                .targetType(consumerType)
                .module(module)
                .options(options)
                .build()
                .proxy();
    }
}
