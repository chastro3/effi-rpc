package io.effi.rpc.component;

import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.ObjectUtil;
import io.effi.rpc.util.StringUtil;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides a default implementation of {@link BeanFactory}.
 */
public class DefaultBeanFactory implements BeanFactory {

    private final Map<Class<?>, Object> singletonBeans = new ConcurrentHashMap<>();
    private final Map<Class<?>, Map<String, Object>> namedBeans = new ConcurrentHashMap<>();
    private ScopedContext owner;

    public DefaultBeanFactory(ScopedContext owner) {
        this.owner = AssertUtil.notNull(owner, "owner");
    }

    @Override
    public boolean containsBean(Class<?> type, String name) {
        ScopedComponentDescriptor descriptor = ensureScopedComponentDescriptor(type);
        if (descriptor.isSingle()) {
            return singletonBeans.containsKey(type);
        } else {
            return namedBeans.containsKey(type) && namedBeans.get(type).containsKey(name);
        }
    }

    @Override
    public <T> BeanFactory registerBean(Class<T> type, String name, T bean) {
        ScopedComponentDescriptor descriptor = ensureScopedComponentDescriptor(type);
        if (descriptor.isSingle()) {
            singletonBeans.put(type, bean);
        } else {
            namedBeans.computeIfAbsent(type, k -> new ConcurrentHashMap<>()).put(name, bean);
        }
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> type) {
        ScopedComponentDescriptor descriptor = ensureScopedComponentDescriptor(type);
        if (descriptor.isSingle()) {
            return (T) singletonBeans.get(type);
        } else {
            Map<String, Object> namedMap = namedBeans.get(type);
            if (CollectionUtil.isEmpty(namedMap)) return null;
            if (namedMap.size() > 1) {
                throw new IllegalStateException("Multiple beans of type " + type + ", use getBean(type, name) instead");
            }
            return (T) namedMap.values().iterator().next();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> type, String name) {
        ScopedComponentDescriptor descriptor = ensureScopedComponentDescriptor(type);
        if (descriptor.isSingle()) {
            return (T) singletonBeans.get(type);
        } else {
            return (T) namedBeans.getOrDefault(type, Collections.emptyMap()).get(name);
        }
    }

    @Override
    public String[] getBeanNames(Class<?> type) {
        ScopedComponentDescriptor descriptor = ensureScopedComponentDescriptor(type);
        if (descriptor.isSingle()) {
            return singletonBeans.containsKey(type)
                    ? new String[]{ObjectUtil.lowercaseName(type)}
                    : StringUtil.emptyArray();
        } else {
            Map<String, Object> map = namedBeans.getOrDefault(type, Collections.emptyMap());
            return map.keySet().toArray(StringUtil.emptyArray());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> getBeans(Class<T> type) {
        ScopedComponentDescriptor descriptor = ensureScopedComponentDescriptor(type);
        if (descriptor.isSingle()) {
            Object bean = singletonBeans.get(type);
            if (bean == null) return Collections.emptyMap();
            return Collections.singletonMap(ObjectUtil.lowercaseName(type), (T) bean);
        } else {
            return (Map<String, T>) namedBeans.getOrDefault(type, Collections.emptyMap());
        }
    }

    @Override
    public BeanFactory removeBean(Class<?> type) {
        ScopedComponentDescriptor descriptor = ensureScopedComponentDescriptor(type);
        if (descriptor.isSingle()) {
            singletonBeans.remove(type);
        } else {
            namedBeans.remove(type);
        }
        return this;
    }

    @Override
    public BeanFactory removeBean(Class<?> type, String name) {
        ScopedComponentDescriptor descriptor = ensureScopedComponentDescriptor(type);
        if (descriptor.isSingle()) {
            singletonBeans.remove(type);
        } else {
            Map<String, Object> map = namedBeans.get(type);
            if (map != null) {
                map.remove(name);
                if (map.isEmpty()) {
                    namedBeans.remove(type);
                }
            }
        }
        return this;
    }

    @Override
    public void clear() {
        singletonBeans.clear();
        namedBeans.clear();
    }

    @Override
    public ScopedContext owner() {
        return owner;
    }

    @Override
    public void setOwner(ScopedContext owner) {
        this.owner = owner;
    }
}

