package io.effi.rpc.spring.support;

import io.effi.rpc.annotation.component.ScopedComponent.Scope;
import io.effi.rpc.component.ComponentDescriptor;
import io.effi.rpc.component.ScopedContext;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Handles registration of Spring-managed scoped components after all singleton contexts are available.
 */
public final class ScopedComponentRegistrar implements BeanPostProcessor, SmartInitializingSingleton {

    private final ApplicationContext applicationContext;

    private final List<ScopedComponentCandidate> candidates = new CopyOnWriteArrayList<>();

    private volatile boolean ready;

    public ScopedComponentRegistrar(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ScopedContext || bean instanceof ScopedComponentRegistrar) {
            return bean;
        }
        List<Class<?>> componentTypes = ComponentDescriptor.findSupportedComponentTypes(AopUtils.getTargetClass(bean));
        if (componentTypes.isEmpty()) {
            return bean;
        }
        ScopedComponentCandidate candidate = new ScopedComponentCandidate(beanName, bean, List.copyOf(componentTypes));
        if (ready) {
            register(candidate);
        } else {
            candidates.add(candidate);
        }
        return bean;
    }

    @Override
    public void afterSingletonsInstantiated() {
        candidates.forEach(this::register);
        candidates.clear();
        ready = true;
    }

    @SuppressWarnings("unchecked")
    private void register(ScopedComponentCandidate candidate) {
        for (Class<?> componentType : candidate.componentTypes()) {
            ComponentDescriptor descriptor = ComponentDescriptor.lookup(componentType);
            Map<String, ? extends ScopedContext> contexts = applicationContext.getBeansOfType(descriptor.scopedContextType());
            if (contexts.isEmpty()) {
                throw new IllegalStateException("No scoped context found for component bean '"
                        + candidate.beanName() + "' and type '" + componentType.getName() + "'");
            }
            if (descriptor.scope() != Scope.UNIVERSAL && contexts.size() > 1) {
                throw new IllegalStateException("Multiple scoped contexts found for component bean '"
                        + candidate.beanName() + "' and type '" + componentType.getName()
                        + "'. Register the component manually when multiple contexts are intentional.");
            }
            for (ScopedContext context : contexts.values()) {
                context.registry().register((Class<Object>) componentType, candidate.beanName(), candidate.bean());
            }
        }
    }

    private record ScopedComponentCandidate(String beanName, Object bean, List<Class<?>> componentTypes) {
    }
}
