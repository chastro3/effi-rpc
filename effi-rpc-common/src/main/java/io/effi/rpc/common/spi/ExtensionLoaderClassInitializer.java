package io.effi.rpc.common.spi;

/**
 * Invoked during the class initialization of {@link ExtensionLoader}.
 */
public interface ExtensionLoaderClassInitializer {

    /**
     * Performs initialization.
     */
    void initialize();
}
