package io.effi.rpc.util.hook;

/**
 * Defines hooks for intercepting start events.
 * <p>
 * Provides callback methods that are invoked during the startup
 * process of target objects, allowing customization before and after
 * the start operation completes.
 */
public interface StartHook<T> extends Hook<T> {

    /**
     * Invoked before the target is started.
     */
    default void onStarting(T target) {
    }

    /**
     * Invoked after the target has been started.
     */
    default void onStarted(T target) {
    }

}
