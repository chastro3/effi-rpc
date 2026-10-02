package io.effi.rpc.context;

/**
 * Wraps a remote service and manages its servants.
 */
public interface ServantGroup<T> extends PeerGroup<Servant, T> {

    /**
     * Returns the id of the service.
     */
    String name();

    /**
     * Returns the service implementation.
     */
    T service();

    /**
     * Retrieves the index of the specified callee.
     *
     * @param servant the callee whose index is to be retrieved
     * @return the index of the callee
     */
    int indexOf(Servant servant);

    /**
     * Invokes one method on the service implementation.
     */
    /**
     * Invokes one servant method and returns its result.
     *
     * @param servant servant to invoke
     * @param args method arguments
     * @param <R> result type
     * @return invocation result
     */
    <R> R invoke(Servant servant, Object... args);

}


