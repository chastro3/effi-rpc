package io.effi.rpc.contract;

/**
 *
 * Wraps a remote service and manages its internal callees.
 */
public interface RemoteService<T> extends InvokerContainer<Callee<?>> {

    /**
     * Returns the wrapped service.
     */
    T service();

    /**
     * Returns the type of the service.
     */
    Class<T> serviceType();

    /**
     * Returns the name of the service.
     */
    String name();

    /**
     * Invokes a callee on the service.
     *
     * @param callee the callee
     * @param args   the arguments
     * @param <R>    the return type of {@link Callee#invoke(Object...)}
     * @return the result of the invocation
     */
    <R> R invokeCallee(Callee<T> callee, Object... args);

    /**
     * Adds a callee to the service.
     *
     * @param callee the callee to add
     * @return the updated remote service instance
     */
    RemoteService<T> addCallee(Callee<?> callee);

    /**
     * Retrieves the index of the specified callee.
     *
     * @param callee the callee whose index is to be retrieved
     * @return the index of the callee
     */
    int getCalleeIndex(Callee<?> callee);

    /**
     * Retrieves the callee for the specified protocol and path.
     *
     * @param protocol the protocol used by the callee
     * @param path     the path used by the callee
     * @return the callee for the given protocol and path
     */
    Callee<?> getCallee(String protocol, String path);
}


