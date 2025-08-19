package io.effi.rpc.util.hook;

/**
 * Defines hooks for intercepting close events.
 * <p>
 * Provides callback methods that are invoked during the closing
 * process of target objects, allowing customization before and after
 * the close operation completes.
 */
public interface CloseHook<T> extends Hook<T> {

    /**
     * Invoked before the target is closed.
     */
    default void onClosing(T target) {
    }

    /**
     * Invoked after the target has been closed.
     */
    default void onClosed(T target) {
    }

}
