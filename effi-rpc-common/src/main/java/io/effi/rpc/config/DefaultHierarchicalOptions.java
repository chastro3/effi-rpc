package io.effi.rpc.config;

import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provides the default implementation of {@link HierarchicalOptions}.
 */
public class DefaultHierarchicalOptions extends DefaultOptions implements HierarchicalOptions {

    protected Options parent;

    protected Object owner;

    public DefaultHierarchicalOptions() {
    }

    public DefaultHierarchicalOptions(int initialCapacity) {
        super(initialCapacity);
    }

    public DefaultHierarchicalOptions(Map<String, Object> items) {
        super(items);
    }

    @Override
    public <V> V option(OptionName<V> name) {
        OptionName.Strategy strategy = name.strategy();
        V value = null;
        if (strategy == OptionName.Strategy.CURRENT_FIRST) {
            value = currentFirstOption(name);
        } else if (strategy == OptionName.Strategy.PARENT_FIRST) {
            value = parentFirstOption(name);
        } else if (strategy == OptionName.Strategy.ONLY_CURRENT) {
            value = super.option(name);
        }
        return value == null ? name.defaultValue() : value;
    }

    @Override
    public <V> List<V> mergedOption(OptionName<V> name) {
        return mergedOption(name.name());
    }

    @Override
    public <V> List<V> mergedOption(String name) {
        List<V> mergedList = new ArrayList<>();
        if (parent != null) {
            if (parent instanceof DefaultHierarchicalOptions options) {
                List<V> values = options.mergedOption(name);
                if (CollectionUtil.isNotEmpty(values)) {
                    mergedList.addAll(values);
                }
            } else {
                V value = parent.option(name);
                if (value != null) {
                    mergedList.add(value);
                }
            }
        }
        V value = super.option(name);
        if (value != null) {
            mergedList.add(value);
        }
        return mergedList;
    }

    public <V> V currentFirstOption(OptionName<V> name) {
        V value = super.option(name);
        if (value == null && parent() != null) {
            if (parent instanceof DefaultHierarchicalOptions options) {
                value = options.currentFirstOption(name);
            } else {
                value = parent.option(name);
            }
            if (value != null) {
                this.addOption(name, value);
            }
        }
        return value;
    }

    public <V> V parentFirstOption(OptionName<V> name) {
        if (parent() != null) {
            V value;
            if (parent instanceof DefaultHierarchicalOptions options) {
                value = options.parentFirstOption(name);
            } else {
                value = parent.option(name);
            }
            if (value != null) {
                return value;
            }
        }
        return super.option(name);
    }

    @Override
    public DefaultHierarchicalOptions withParent(Options parent) {
        Object owner = null;
        if (parent instanceof HierarchicalOptions options) {
            owner = options.owner();
        }
        if (owner != this) {
            this.parent = parent;
        }
        return this;
    }

    @Override
    public Options parent() {
        return parent;
    }

    @Override
    public DefaultHierarchicalOptions withOwner(Object owner) {
        if (this.owner != owner)
            this.owner = owner;
        return this;
    }

    @Override
    public Object owner() {
        return owner;
    }

    @Override
    public String toString() {
        return StringUtil.format("size={}, hasOwner={}, hasParent={}", items.size(), owner != null, parent != null);
    }
}
