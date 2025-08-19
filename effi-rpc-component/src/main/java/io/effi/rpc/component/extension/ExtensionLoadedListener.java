package io.effi.rpc.component.extension;

import io.effi.rpc.annotation.component.ScopedComponent;

import java.util.EventListener;

/**
 * Handles notifications after extension instances are created.
 * <p>
 * Implementations of this interface receive callbacks when extensions are loaded,
 * allowing for post-processing or initialization logic to be executed.
 */
@ScopedComponent
public interface ExtensionLoadedListener<T> extends EventListener {

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




