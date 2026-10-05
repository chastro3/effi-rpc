package io.effi.rpc.spring.bean;

import io.effi.rpc.boot.AnnotationCallerGroup;
import io.effi.rpc.boot.InterfaceCallerGroup;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.option.HierarchicalOptions;
import io.effi.rpc.spring.properties.EffiRpcProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;

/**
 * Builds and caches one consumer proxy for an interface.
 */
public final class EffiRpcConsumerFactoryBean<T> implements FactoryBean<T>, BeanFactoryAware {

    private final Class<T> consumerType;

    private final EffiRpcConsumerMode mode;

    private BeanFactory beanFactory;

    public EffiRpcConsumerFactoryBean(Class<T> consumerType, EffiRpcConsumerMode mode) {
        this.consumerType = consumerType;
        this.mode = mode;
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
        EffiRpcConsumerTargetResolver.ResolvedTarget target =
                EffiRpcConsumerTargetResolver.resolve(consumerType, properties.consumer());
        HierarchicalOptions options = HierarchicalOptions.create();
        EffiRpcConsumerOptions.apply(options, properties.consumer(), target);
        ScopedModule module = beanFactory.getBean(ScopedModule.class);

        if (mode == EffiRpcConsumerMode.ANNOTATION) {
            return AnnotationCallerGroup.<T>builder()
                    .targetType(consumerType)
                    .module(module)
                    .options(options)
                    .build()
                    .proxy();
        }
        return InterfaceCallerGroup.<T>builder()
                .targetType(consumerType)
                .module(module)
                .options(options)
                .build()
                .proxy();
    }
}
