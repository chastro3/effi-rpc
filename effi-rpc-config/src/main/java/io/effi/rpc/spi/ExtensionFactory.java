package io.effi.rpc.spi;

import io.effi.rpc.util.Ordered;

/**
 * Provides extensions using reflection or dependency injection.
 */
public interface ExtensionFactory extends Ordered {

    /**
     * Returns an extension instance for the given type and names.
     */
    <T> T getExtension(Class<T> type, String[] names);
}




