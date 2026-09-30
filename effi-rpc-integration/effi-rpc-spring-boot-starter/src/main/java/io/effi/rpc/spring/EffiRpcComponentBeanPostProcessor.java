package io.effi.rpc.spring;

import io.effi.rpc.component.ComponentDescriptor;
import io.effi.rpc.component.ScopedContext;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;
import java.util.Map;

public class EffiRpcComponentBeanPostProcessor implements ApplicationContextAware, BeanPostProcessor {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ScopedContext) {
            return bean;
        }
        Class<?> beanType = AopUtils.getTargetClass(bean);
        List<Class<?>> scopedComponentTypes = ComponentDescriptor.findSupportedComponentTypes(beanType);
        for (Class<?> scopedComponentType : scopedComponentTypes) {
            ComponentDescriptor descriptor = ComponentDescriptor.lookup(scopedComponentType);
            Map<String, ? extends ScopedContext> scopedContexts =
                    applicationContext.getBeansOfType(descriptor.scopedContextType());
            for (ScopedContext scopedContext : scopedContexts.values()) {
                scopedContext.registry().register((Class<Object>) scopedComponentType, beanName, bean);
            }
        }
        return bean;
    }
}
