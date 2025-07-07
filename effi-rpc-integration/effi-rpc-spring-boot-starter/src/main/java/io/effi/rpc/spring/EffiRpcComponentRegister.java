package io.effi.rpc.spring;


import io.effi.rpc.component.ScopedComponentDescriptor;
import io.effi.rpc.component.ScopedContext;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;
import java.util.Map;

public class EffiRpcComponentRegister implements ApplicationContextAware, BeanPostProcessor {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanType = AopUtils.getTargetClass(bean);
        List<Class<?>> scopedComponentTypes = ScopedContext.findMatchingScopedComponentTypes(beanType);
        if (!scopedComponentTypes.isEmpty()) {
            for (Class<?> scopedComponentType : scopedComponentTypes) {
                ScopedComponentDescriptor descriptor = ScopedContext.getScopedComponentDescriptor(scopedComponentType);
                Map<String, ? extends ScopedContext> beansOfType = applicationContext.getBeansOfType(descriptor.scopedContextType());
                for (ScopedContext scopedContext : beansOfType.values()) {
                    if (!(bean instanceof ScopedContext))
                        scopedContext.register((Class<Object>) scopedComponentType, beanName, bean);
                }
            }
        }
        return bean;
    }
}
