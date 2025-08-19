package io.effi.rpc.component;

import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.GenericKey;
import io.effi.rpc.util.ObjectUtil;
import io.effi.rpc.util.StringUtil;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiPredicate;

/**
 * Delegates component operations to an underlying {@link BeanFactory}.
 * <p>
 * Provides component repository functionality by delegating registration,
 * lookup, and removal operations to a bean factory within a scoped context.
 */
public class DelegateComponentRepository implements ComponentRepository, ScopedContextOwned {

    private final BeanFactory beanFactory;

    private ScopedContext owner;

    public DelegateComponentRepository() {
        this(new DefaultBeanFactory());
    }

    public DelegateComponentRepository(BeanFactory beanFactory) {
        this(null, beanFactory);
    }

    public DelegateComponentRepository(ScopedContext owner) {
        this(owner, new DefaultBeanFactory(owner));
    }

    public DelegateComponentRepository(ScopedContext owner, BeanFactory beanFactory) {
        this.owner = owner;
        this.beanFactory = AssertUtil.notNull(beanFactory, "bean factory");
    }

    @Override
    public <T> DelegateComponentRepository register(Class<T> type, T component) {
        String name = ObjectUtil.resolveName(component);
        return register(type, name, component);
    }

    @Override
    public <T> DelegateComponentRepository register(Class<T> type, String name, T component) {
        if (StringUtil.isBlank(name)) name = ObjectUtil.resolveName(component);
        beanFactory.registerBean(type, name, component);
        return this;
    }

    @Override
    public <T> T singleComponent(Class<T> type) {
        return beanFactory.getBean(type);
    }

    @Override
    public <T> T namedComponent(Class<T> type, String name) {
        return beanFactory.getBean(type, name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T externalComponent(GenericKey<T> name) {
        ExternalComponent<T> externalComponent =
                (ExternalComponent<T>) beanFactory.getBean(ExternalComponent.class, name.name());
        if (externalComponent != null) {
            return externalComponent.value();
        }
        return null;
    }

    @Override
    public int componentCount(Class<?> type) {
        return beanFactory.getBeanNames(type).length;
    }

    @Override
    public <T> Collection<T> components(Class<T> type, BiPredicate<String, T> filter) {
        return namedComponents(type, filter).values();
    }

    @Override
    public <T> Map<String, T> namedComponents(Class<T> type, BiPredicate<String, T> filter) {
        Map<String, T> beans = beanFactory.getBeans(type);
        return CollectionUtil.unmodifiable(beans, filter);
    }

    @Override
    public DelegateComponentRepository remove(Class<?> type) {
        beanFactory.removeBean(type);
        return this;
    }

    @Override
    public DelegateComponentRepository remove(Class<?> type, String name) {
        beanFactory.removeBean(type, name);
        return this;
    }

    @Override
    public void withOwner(ScopedContext owner) {
        this.owner = owner;
        beanFactory.withOwner(owner);
    }

    @Override
    public ScopedContext owner() {
        return owner;
    }

    @Override
    public void clear() {
        beanFactory.clear();
    }

}
