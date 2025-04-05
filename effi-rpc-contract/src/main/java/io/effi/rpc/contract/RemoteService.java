package io.effi.rpc.contract;

/**
 * Wrapper service for interacting with a remote service.
 * <p>
 * Provides methods to access the service instance, its type, and invoke methods on it.
 * The service can have multiple callees, each representing a specific endpoint.
 *
 * @param <T> the type of the remote service interface
 */
public interface RemoteService<T> extends InvokerContainer<Callee<?>> {

    /**
     * Returns the target instance of the remote service.
     */
    T target();

    /**
     * Returns the class type of the remote service.
     */
    Class<T> targetType();

    /**
     * Returns the name of the remote service.
     */
    String name();

    /**
     * Invokes a method on the remote service.
     *
     * @param callee the callee representing the method to invoke
     * @param args   the arguments to pass to the method
     * @param <R>    the return type of {@link Callee#invoke(Object...)}
     * @return the result of the invocation
     */
    <R> R invokeCallee(Callee<T> callee, Object... args);

    /**
     * Adds a callee to the remote service.
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


