package io.effi.rpc.context.invocation;

import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.util.GenericKey;

/**
 * Defines protocol-neutral invocation attribute keys.
 */
public interface InvocationKeys {

    GenericKey<ScopedModule> MODULE = GenericKey.valueOf("invocation.module");
}
