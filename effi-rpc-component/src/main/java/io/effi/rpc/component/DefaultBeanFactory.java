package io.effi.rpc.component;

import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.ObjectUtil;
import io.effi.rpc.util.StringUtil;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides the default implementation of {@link BeanFactory}.
 */
public class DefaultBeanFactory implements BeanFactory {

    private final Map<Class<?>, Object> singleBeans = new ConcurrentHashMap<>();

    private final Map<Class<?>, Map<String, Object>> namedBeans = new ConcurrentHashMap<>();

    private ScopedContext owner;

    public DefaultBeanFactory() {
    }

    public DefaultBeanFactory(ScopedContext owner) {
        this.owner = AssertUtil.notNull(owner, "owner");
    }

    @Override
    public boolean containsBean(Class<?> type, String name) {
        ComponentDescriptor descriptor = ensureComponentDescriptor(type);
        if (descriptor.single()) {
            return singleBeans.containsKey(type);
        } else {
            return namedBeans.containsKey(type) && namedBeans.get(type).containsKey(name);
        }
    }

    @Override
    public <T> BeanFactory registerBean(Class<T> type, String name, T bean) {
        ComponentDescriptor descriptor = ensureComponentDescriptor(type);
        if (descriptor.single()) {
            singleBeans.put(type, bean);
        } else {
            namedBeans.computeIfAbsent(type, k -> new ConcurrentHashMap<>()).put(name, bean);
        }
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> type) {
        ComponentDescriptor descriptor = ensureComponentDescriptor(type);
        if (descriptor.single()) {
            return (T) singleBeans.get(type);
        } else {
            Map<String, Object> namedMap = namedBeans.get(type);
            if (CollectionUtil.isEmpty(namedMap)) return null;
            if (namedMap.size() > 1) {
                throw new IllegalStateException("Multiple beans of type " + type + ", use getBean(type, id) instead");
            }
            return (T) namedMap.values().iterator().next();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> type, String name) {
        ComponentDescriptor descriptor = ensureComponentDescriptor(type);
        if (descriptor.single()) {
            return (T) singleBeans.get(type);
        } else {
            return (T) namedBeans.getOrDefault(type, Collections.emptyMap()).get(name);
        }
    }

    @Override
    public String[] getBeanNames(Class<?> type) {
        ComponentDescriptor descriptor = ensureComponentDescriptor(type);
        if (descriptor.single()) {
            return singleBeans.containsKey(type)
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
        ComponentDescriptor descriptor = ensureComponentDescriptor(type);
        if (descriptor.single()) {
            Object bean = singleBeans.get(type);
            if (bean == null) return Collections.emptyMap();
            return Collections.singletonMap(ObjectUtil.lowercaseName(type), (T) bean);
        } else {
            return (Map<String, T>) namedBeans.getOrDefault(type, Collections.emptyMap());
        }
    }

    @Override
    public BeanFactory removeBean(Class<?> type) {
        ComponentDescriptor descriptor = ensureComponentDescriptor(type);
        if (descriptor.single()) {
            singleBeans.remove(type);
        } else {
            namedBeans.remove(type);
        }
        return this;
    }

    @Override
    public BeanFactory removeBean(Class<?> type, String name) {
        ComponentDescriptor descriptor = ensureComponentDescriptor(type);
        if (descriptor.single()) {
            singleBeans.remove(type);
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
        releaseAll(singleBeans);
        singleBeans.clear();
        namedBeans.values().forEach(this::releaseAll);
        namedBeans.clear();
    }

    @Override
    public ScopedContext owner() {
        return owner;
    }

    @Override
    public void withOwner(ScopedContext owner) {
        this.owner = owner;
    }

    private void releaseAll(Map<?, ?> map) {
        map.values().forEach(ObjectUtil::release);
    }
}

