package io.effi.rpc.component;

import java.util.EnumMap;

import static io.effi.rpc.annotation.component.ScopedComponent.Kind;
import static io.effi.rpc.annotation.component.ScopedComponent.Scope;

/**
 * Describes a component's scope, kind, and its scoped context type.
 */
public class ScopedComponentDescriptor {

    private static final EnumMap<Scope, EnumMap<Kind, ScopedComponentDescriptor>> CACHE = new EnumMap<>(Scope.class);

    static {
        for (Scope s : Scope.values()) {
            EnumMap<Kind, ScopedComponentDescriptor> inner = new EnumMap<>(Kind.class);
            for (Kind k : Kind.values()) {
                inner.put(k, new ScopedComponentDescriptor(s, k));
            }
            CACHE.put(s, inner);
        }
    }

    private final Scope scope;
    private final Kind kind;

    ScopedComponentDescriptor(Scope scope, Kind kind) {
        this.scope = scope;
        this.kind = kind;
    }

    public static ScopedComponentDescriptor valueOf(Scope scope, Kind kind) {
        return CACHE.get(scope).get(kind);
    }

    public Scope scope() {
        return scope;
    }

    public Kind kind() {
        return kind;
    }

    public Class<? extends ScopedContext> scopedContextType() {
        return switch (scope) {
            case PLATFORM -> EffiRpcPlatform.class;
            case APPLICATION -> EffiRpcApplication.class;
            case MODULE -> EffiRpcModule.class;
            default -> ScopedContext.class;
        };
    }

    public boolean isSingle() {
        return kind == Kind.SINGLE;
    }


}