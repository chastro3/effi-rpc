package io.effi.rpc.spi;

/**
 * Listener triggered after an extension instance is created.
 *
 * @param <T> the service type for the extension
 */
@FunctionalInterface
public interface LoadedListener<T> {

    /**
     * Called after an extension instance is loaded.
     */
    void onLoaded(T service);
}



