package io.effi.rpc.spi;

/**
 * Initializes during {@link ExtensionLoader} class loading.
 */
public interface ExtensionLoaderClassInitializer {

    /**
     * Performs initialization.
     */
    void initialize();
}
