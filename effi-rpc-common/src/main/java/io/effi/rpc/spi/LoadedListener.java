package io.effi.rpc.spi;

/**
 * Listener triggered after an extension instance is created.
 */
@FunctionalInterface
public interface LoadedListener<T> {

    /**
     * Invoked after an extension instance is loaded.
     */
    void onLoaded(T extension);
}



