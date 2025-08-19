package io.effi.rpc.config;

import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides the default implementation of {@link HierarchicalConfig}.
 */
public class DefaultHierarchicalConfig extends DefaultConfig implements HierarchicalConfig {

    protected Config parent;

    public DefaultHierarchicalConfig() {
        this(null);
    }

    public DefaultHierarchicalConfig(Object owner) {
        this(owner, null);
    }

    public DefaultHierarchicalConfig(Object owner, Config parent) {
        super(owner);
        if (parent != null) {
            withParent(parent);
        }
    }

    @Override
    public <V> V get(ConfigName<V> name) {
        ConfigName.Strategy strategy = name.strategy();
        V value = null;
        if (strategy == ConfigName.Strategy.CURRENT_FIRST) {
            value = getCurrentFirst(name);
        } else if (strategy == ConfigName.Strategy.PARENT_FIRST) {
            value = getParentFirst(name);
        } else if (strategy == ConfigName.Strategy.ONLY_CURRENT) {
            value = super.get(name);
        }
        return value == null ? name.defaultValue() : value;
    }

    @Override
    public <V> List<V> getMerged(ConfigName<V> name) {
        return getMerged(name.name());
    }

    @Override
    public <V> List<V> getMerged(String name) {
        List<V> mergedList = new ArrayList<>();
        if (parent != null) {
            if (parent instanceof DefaultHierarchicalConfig nodeConfig) {
                List<V> values = nodeConfig.getMerged(name);
                if (CollectionUtil.isNotEmpty(values)) {
                    mergedList.addAll(values);
                }
            } else {
                V value = parent.get(name);
                if (value != null) {
                    mergedList.add(value);
                }
            }
        }
        V value = super.get(name);
        if (value != null) {
            mergedList.add(value);
        }
        return mergedList;
    }

    public <V> V getCurrentFirst(ConfigName<V> name) {
        V value = super.get(name);
        if (value == null && parent() != null) {
            if (parent instanceof DefaultHierarchicalConfig nodeConfig) {
                value = nodeConfig.getCurrentFirst(name);
            } else {
                value = parent.get(name);
            }
            if (value != null) {
                this.set(name, value);
            }
        }
        return value;
    }

    public <V> V getParentFirst(ConfigName<V> name) {
        if (parent() != null) {
            V value;
            if (parent instanceof DefaultHierarchicalConfig nodeConfig) {
                value = nodeConfig.getParentFirst(name);
            } else {
                value = parent.get(name);
            }
            if (value != null) {
                return value;
            }
        }
        return super.get(name);
    }

    @Override
    public void withParent(Config parent) {
        if (parent.owner() != this) {
            this.parent = parent;
        }
    }

    @Override
    public Config parent() {
        return parent;
    }

    @Override
    public String toString() {
        return StringUtil.format("size={}, hasOwner={}, hasParent={}", items.size(), owner != null, parent != null);
    }
}
