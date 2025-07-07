package io.effi.rpc.config;

import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
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
    public String get(ConfigName configName) {
        String key = configName.realName();
        ConfigName.Strategy strategy = configName.strategy();
        String value = null;
        if (strategy == ConfigName.Strategy.SELF_PREFERRED) {
            value = getSelfPreferred(key);
        } else if (strategy == ConfigName.Strategy.PARENT_PREFERRED) {
            value = getParentPreferred(key);
        } else if (strategy == ConfigName.Strategy.CASCADED) {
            List<String> values = getCascaded(key);
            if (CollectionUtil.isNotEmpty(values)) {
                value = String.join(configName.separator(), values);
            }
        } else {
            value = get(key);
        }
        return StringUtil.isBlank(value) ? configName.defaultValue() : value;
    }

    @Override
    public List<String> getCascaded(ConfigName configName) {
        return configName.strategy() == ConfigName.Strategy.CASCADED
                ? getCascaded(configName.realName())
                : Collections.emptyList();
    }

    @Override
    public List<String> getCascaded(String key) {
        List<String> mergedList = new ArrayList<>();
        if (parent != null) {
            if (parent instanceof HierarchicalNodeConfig nodeConfig) {
                List<String> values = nodeConfig.getCascaded(key);
                if (CollectionUtil.isNotEmpty(values)) {
                    mergedList.addAll(values);
                }
            } else {
                String value = parent.get(key);
                if (StringUtil.isNotBlank(value)) {
                    mergedList.add(value);
                }
            }
        }
        String value = this.get(key);
        if (value != null) {
            mergedList.add(value);
        }
        return mergedList;
    }

    public String getSelfPreferred(String key) {
        String value = this.get(key);
        if (value == null && parent() != null) {
            if (parent instanceof HierarchicalNodeConfig nodeConfig) {
                value = nodeConfig.getSelfPreferred(key);
            } else {
                value = parent.get(key);
            }
            if (value != null) {
                this.set(key, value);
            }
        }
        return value;
    }

    public String getParentPreferred(String key) {
        if (parent() != null) {
            String value;
            if (parent instanceof HierarchicalNodeConfig nodeConfig) {
                value = nodeConfig.getParentPreferred(key);
            } else {
                value = parent.get(key);
            }
            if (value != null) {
                return value;
            }
        }
        return this.get(key);
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
