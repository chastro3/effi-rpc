package io.effi.rpc.context.parameter;

/**
 * Defines a no-argument method reference callback.
 */
@FunctionalInterface
public interface MethodRef {

    /**
     * Invokes the referenced method.
     */
    void call();
}
