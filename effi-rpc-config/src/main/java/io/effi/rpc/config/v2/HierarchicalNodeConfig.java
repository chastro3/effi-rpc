package io.effi.rpc.config.v2;

import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a key-value configuration with hierarchy.
 */
public class HierarchicalNodeConfig extends FlatConfig implements NodeConfig {

    protected Config parent;

    public HierarchicalNodeConfig() {
        this(null);
    }

    public HierarchicalNodeConfig(Object owner) {
        this(owner, null);
    }

    public HierarchicalNodeConfig(Object owner, Config parent) {
        super(owner);
        if (parent != null) {
            setParent(parent);
        }
    }

    @Override
    public <V> V get(ConfigName<V> name) {
        ConfigName.Strategy strategy = name.strategy();
        V value = null;
        if (strategy == ConfigName.Strategy.SELF_PREFERRED) {
            value = getSelfPreferred(name);
        } else if (strategy == ConfigName.Strategy.PARENT_PREFERRED) {
            value = getParentPreferred(name);
        } else if (strategy == ConfigName.Strategy.SELF_ONLY) {
            value = super.get(name);
        }
        return value == null ? name.defaultValue() : value;
    }

    @Override
    public <V> List<V> getCascaded(ConfigName<V> name) {
        List<V> mergedList = new ArrayList<>();
        if (parent != null) {
            if (parent instanceof HierarchicalNodeConfig nodeConfig) {
                List<V> values = nodeConfig.getCascaded(name);
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
        V value = this.get(name);
        if (value != null) {
            mergedList.add(value);
        }
        return mergedList;
    }

    public <V> V getSelfPreferred(ConfigName<V> name) {
        V value = this.get(name);
        if (value == null && parent() != null) {
            if (parent instanceof HierarchicalNodeConfig nodeConfig) {
                value = nodeConfig.getSelfPreferred(name);
            } else {
                value = parent.get(name);
            }
            if (value != null) {
                this.set(name, value);
            }
        }
        return value;
    }

    public <V> V getParentPreferred(ConfigName<V> name) {
        if (parent() != null) {
            V value;
            if (parent instanceof HierarchicalNodeConfig nodeConfig) {
                value = nodeConfig.getParentPreferred(name);
            } else {
                value = parent.get(name);
            }
            if (value != null) {
                return value;
            }
        }
        return this.get(name);
    }

    public void setParent(Config parent) {
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
