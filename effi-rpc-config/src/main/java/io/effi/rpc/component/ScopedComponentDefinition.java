package io.effi.rpc.component;

import java.util.EnumMap;

import static io.effi.rpc.annotation.component.ScopedComponent.Kind;
import static io.effi.rpc.annotation.component.ScopedComponent.Scope;

/**
 * Defines scope and kind for components.
 */
public record ScopedComponentDefinition(Scope scope, Kind kind) {

    private static final EnumMap<Scope, EnumMap<Kind, ScopedComponentDefinition>> CACHE = new EnumMap<>(Scope.class);

    static ScopedComponentDefinition DEFAULT;

    static {
        for (Scope s : Scope.values()) {
            EnumMap<Kind, ScopedComponentDefinition> inner = new EnumMap<>(Kind.class);
            for (Kind k : Kind.values()) {
                inner.put(k, new ScopedComponentDefinition(s, k));
            }
            CACHE.put(s, inner);
        }
        DEFAULT = valueOf(Scope.UNIVERSAL, Kind.MULTI);
    }

    public static ScopedComponentDefinition valueOf(Scope scope, Kind kind) {
        return CACHE.get(scope).get(kind);
    }

}