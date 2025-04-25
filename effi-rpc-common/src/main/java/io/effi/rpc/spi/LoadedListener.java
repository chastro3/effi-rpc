package io.effi.rpc.spi;

/**
 * SPI listener invoked when an extension instance is created,
 * allowing for post-creation actions.
 *
 * @param <T> the service type for the extension
 */
@FunctionalInterface
public interface LoadedListener<T> {

    /**
     * Invoked after an extension instance is created, allowing additional setup or operations.
     *
     * @param service the created service instance
     */
    void onLoaded(T service);
}


