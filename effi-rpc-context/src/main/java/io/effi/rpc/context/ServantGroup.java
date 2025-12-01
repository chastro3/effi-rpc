package io.effi.rpc.context;

/**
 * Wraps a remote service and manages its internal callee(s).
 */
public interface ServantGroup<T> extends PeerGroup<Servant, T> {

    /**
     * Returns the id of the service.
     */
    String name();

    /**
     * Retrieves the index of the specified callee.
     *
     * @param servant the callee whose index is to be retrieved
     * @return the index of the callee
     */
    int indexOf(Servant servant);

}


