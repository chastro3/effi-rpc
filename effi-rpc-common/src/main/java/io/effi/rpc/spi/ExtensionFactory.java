package io.effi.rpc.spi;

import io.effi.rpc.util.Ordered;

import java.util.List;

/**
 * Factory for creating extensions of a specified type and name(s).
 * Allows various mechanisms for object creation, such as reflection or dependency injection.
 */
public interface ExtensionFactory extends Ordered {

    /**
     * Gets an extension instance of the specified type and name(s).
     *
     * @param type  the type of the extension
     * @param names the names of the extension
     * @param <T>   the type of the object to create
     * @return an instance of the specified type, or {@link null} if creation fails
     */
    <T> T getExtension(Class<T> type, List<String> names);
}


