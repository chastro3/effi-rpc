package io.effi.rpc.core;

import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.context.Peer;
import io.effi.rpc.context.PeerGroup;
import io.effi.rpc.context.options.PeerOptions;
import io.effi.rpc.option.HierarchicalOptions;
import io.effi.rpc.option.OptionName;
import io.effi.rpc.trait.FluentBuilder;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.StringUtil;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Provides an abstract implementation of {@link PeerGroup}.
 */
public abstract class AbstractPeerGroup<P extends Peer, T> implements PeerGroup<P, T> {

    protected Class<T> targetType;

    protected final Map<String, P> values = new LinkedHashMap<>();

    protected HierarchicalOptions options;

    protected AbstractPeerGroup() {
    }

    protected AbstractPeerGroup(Builder<?, P, T, ?> builder) {
        this.targetType = builder.targetType;
        this.options = builder.options.withOwner(this);
    }

    @Override
    public Class<T> targetType() {
        return targetType;
    }

    @Override
    public void register(P peer) {
        values.put(peer.id(), peer);
        peer.options().withParent(this);
    }

    @Override
    public P lookup(String id) {
        return values.get(id);
    }

    @Override
    public HierarchicalOptions options() {
        return options;
    }

    @Override
    public Collection<P> values() {
        return Collections.unmodifiableCollection(values.values());
    }

    protected void onInitialized(Class<T> targetType) {
        this.targetType = targetType;
    }

    /**
     * Assembles a complete {@link PeerGroup} before exposing it.
     */
    public abstract static class Builder<G extends AbstractPeerGroup<P, T>, P extends Peer, T, SELF extends Builder<G, P, T, SELF>>
            implements FluentBuilder<G, SELF>, HierarchicalOptions.Supplier, ScopedModule.Supplier {

        protected Class<T> targetType;

        protected HierarchicalOptions options = HierarchicalOptions.create();

        protected ScopedModule module;

        protected String name;

        protected Builder() {
        }

        public SELF targetType(Class<T> targetType) {
            this.targetType = targetType;
            return self();
        }

        public SELF options(HierarchicalOptions options) {
            this.options = AssertUtil.notNull(options, "options");
            return self();
        }

        @Override
        public <V> SELF addOption(OptionName<V> name, V value) {
            options.addOption(name, value);
            return self();
        }

        public SELF module(ScopedModule module) {
            this.module = AssertUtil.notNull(module, "module");
            return self();
        }

        @Override
        public HierarchicalOptions options() {
            return options;
        }

        @Override
        public ScopedModule module() {
            return module;
        }

        @Override
        public final G build() {
            validate();
            resolve();
            prepare();
            G group = newInstance();
            resolveComponents(group);
            checkState(group);
            return group;
        }

        protected void validate() {
            AssertUtil.notNull(module, "module");
            AssertUtil.notNull(targetType, "targetType");
        }

        protected void resolve() {
        }

        protected void prepare() {
        }

        protected abstract G newInstance();

        protected abstract void resolveComponents(G group);

        protected void checkState(G group) {
            AssertUtil.notNull(group.targetType(), "targetType");
            AssertUtil.notNull(group.options(), "options");
        }

        protected final ScopedModule resolveModule(HierarchicalOptions options) {
            String moduleName = options.option(PeerOptions.ASSOCIATED_MODULE);
            if (StringUtil.isBlank(moduleName)) {
                return module;
            }
            ScopedModule resolved = module.application().lookupModule(moduleName);
            return resolved == null ? module : resolved;
        }

        protected final String methodPath(Method method) {
            return targetType.getName() + "/" + method.getName();
        }
    }

}
