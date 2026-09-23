package io.effi.rpc.option;

import io.effi.rpc.util.StringUtil;

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
        V value = resolveOption(name);
        return value == null ? name.defaultValue() : value;
    }

    /**
     * Resolves the current option before falling back to its parent.
     *
     * @param name option name
     * @return resolved option value
     */
    public <V> V currentFirstOption(OptionName<V> name) {
        V current = currentOption(name);
        return current != null ? current : parentValue(name);
    }

    /**
     * Resolves the parent option before falling back to the current option.
     *
     * @param name option name
     * @return resolved option value
     */
    public <V> V parentFirstOption(OptionName<V> name) {
        V parentValue = parentValue(name);
        return parentValue != null ? parentValue : currentOption(name);
    }

    @SuppressWarnings("unchecked")
    private <V> V mergeParentOption(OptionName<V> name) {
        if (!(name.type() instanceof ArrayOptionType<?> arrayType)) {
            throw new IllegalStateException(
                    "MERGE_PARENT requires ArrayOptionType: " + name.name()
            );
        }
        V currentValue = currentOption(name);
        V parentValue = parentValue(name);
        return (V) ((ArrayOptionType<Object>) arrayType).merge(parentValue, currentValue);
    }

    private <V> V resolveOption(OptionName<V> name) {
        return switch (name.strategy()) {
            case ONLY_CURRENT -> currentOption(name);
            case CURRENT_FIRST -> currentFirstOption(name);
            case PARENT_FIRST -> parentFirstOption(name);
            case MERGE_PARENT -> mergeParentOption(name);
        };
    }

    private <V> V parentValue(OptionName<V> name) {
        if (parent == null) {
            return null;
        }
        if (parent instanceof DefaultHierarchicalOptions options) {
            return options.resolveOption(name);
        }
        if (parent instanceof DefaultOptions options) {
            return options.currentOption(name);
        }
        Object value = parent.items().get(name.name());
        return value == null ? null : name.type().convert(value);
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
        if (this.owner != owner) {
            this.owner = owner;
        }
        return this;
    }

    @Override
    public Object owner() {
        return owner;
    }

    @Override
    public String toString() {
        return StringUtil.format(
                "size={}, hasOwner={}, hasParent={}",
                items.size(), owner != null, parent != null
        );
    }
}
