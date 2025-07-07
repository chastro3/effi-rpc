package io.effi.rpc.component;

import io.effi.rpc.annotation.component.ScopedComponent;

/**
 * Notifies listeners after extension instances are created.
 */
@ScopedComponent
public interface ExtensionLoadedListener<T> {

    /**
     * Handles post-creation event of an extension instance.
     *
     * @param extension the loaded extension instance
     */
    void onLoaded(T extension);

    /**
     * Returns the type of extension this listener handles.
     */
    Class<T> extensionType();
}




