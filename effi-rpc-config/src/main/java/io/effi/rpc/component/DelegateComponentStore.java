package io.effi.rpc.component;

import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.GenericKey;
import io.effi.rpc.util.resoruce.Cleanable;
import io.effi.rpc.util.resoruce.Closeable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;

/**
 * Delegates component operations to an underlying {@link BeanFactory} within a scoped context.
 */
public class DelegateComponentStore implements ComponentStore, ScopedContextOwned {

    private final BeanFactory beanFactory;
    private final Set<Object> needClearedResources = new HashSet<>();
    private ScopedContext owner;

    public DelegateComponentStore(BeanFactory beanFactory) {
        this.beanFactory = AssertUtil.notNull(beanFactory, "bean factory");
    }

    public DelegateComponentStore(ScopedContext owner) {
        this(owner, new DefaultBeanFactory(owner));
    }

    public DelegateComponentStore(ScopedContext owner, BeanFactory beanFactory) {
        this.owner = AssertUtil.notNull(owner, "owner");
        this.beanFactory = AssertUtil.notNull(beanFactory, "bean factory");
    }

    @Override
    public <T> ComponentStore register(Class<T> type, T component) {
        String name = ComponentStore.acquireName(component);
        return register(type, name, component);
    }

    @Override
    public <T> ComponentStore register(Class<T> type, String name, T component) {
        beanFactory.registerBean(type, name, component);
        tryRegisterCleanableComponent(component);
        return this;
    }

    @Override
    public <T> T lookup(Class<T> type) {
        return beanFactory.getBean(type);
    }

    @Override
    public <T> T lookup(Class<T> type, String name) {
        return beanFactory.getBean(type, name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T lookupWrapped(GenericKey<T> name) {
        WrappedComponent<T> wrappedComponent = (WrappedComponent<T>) beanFactory.getBean(WrappedComponent.class, name.name());
        if (wrappedComponent != null) {
            return wrappedComponent.value();
        }
        return null;
    }

    @Override
    public int sizeOf(Class<?> type) {
        return beanFactory.getBeanNames(type).length;
    }

    @Override
    public <T> Collection<T> listOf(Class<T> type, BiPredicate<String, T> filter) {
        return mapOf(type, filter).values();
    }

    @Override
    public <T> Map<String, T> mapOf(Class<T> type, BiPredicate<String, T> filter) {
        Map<String, T> beans = beanFactory.getBeans(type);
        return CollectionUtil.unmodifiable(beans, filter);
    }


    @Override
    public ComponentStore remove(Class<?> type) {
        beanFactory.removeBean(type);
        return this;
    }

    @Override
    public ComponentStore remove(Class<?> type, String name) {
        beanFactory.removeBean(type, name);
        return this;
    }

    public void tryRegisterCleanableComponent(Object component) {
        if (component instanceof Cleanable || component instanceof Closeable) {
            needClearedResources.add(component);
        }
    }

    @Override
    public void clear() {
        needClearedResources.forEach(resource -> owner.tryClearResource(resource));
        beanFactory.clear();
    }

    @Override
    public void setOwner(ScopedContext owner) {
        this.owner = owner;
        beanFactory.setOwner(owner);
    }

    @Override
    public ScopedContext owner() {
        return owner;
    }

}
