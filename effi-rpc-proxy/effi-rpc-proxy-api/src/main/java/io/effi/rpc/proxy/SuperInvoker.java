package io.effi.rpc.proxy;

/**
 * Invokes the original method.
 *
 * @param <R> The return type of the method.
 */
@FunctionalInterface
public interface SuperInvoker<R> {

    /**
     * Executes the original method and returns the result.
     *
     * @return the result of the method invocation
     * @throws Throwable if an error occurs during execution
     */
    R invoke() throws Throwable;
}


