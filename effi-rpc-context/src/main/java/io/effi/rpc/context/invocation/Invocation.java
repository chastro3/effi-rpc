package io.effi.rpc.context.invocation;

import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.util.Attributes;

/**
 * Represents one caller-side invocation.
 */
public interface Invocation extends Attributes.Supplier, ScopedModule.Supplier {

    InvocationArguments arguments();

    @Override
    InvocationAttributes attributes();

    @Override
    default ScopedModule module() {
        return get(InvocationKeys.MODULE);
    }
}
