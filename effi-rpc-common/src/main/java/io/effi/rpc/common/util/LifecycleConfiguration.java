package io.effi.rpc.common.util;

/**
 * Configuration for managing the lifecycle of an object.
 * Provides default hooks for each lifecycle phase: pre-initialization,
 * post-initialization, pre-start, post-start, pre-stop, and post-stop.
 *
 * @param <T> the type of the lifecycle object
 */
public interface LifecycleConfiguration<T extends Lifecycle> {

    /**
     * Hook to perform actions before the lifecycle object is initialized.
     */
    default void preInit(T lifecycle) {
    }

    /**
     * Hook to perform actions after the lifecycle object is initialized.
     */
    default void postInit(T lifecycle) {
    }

    /**
     * Hook to perform actions before the lifecycle object is started.
     */
    default void preStart(T lifecycle) {
    }

    /**
     * Hook to perform actions after the lifecycle object has started.
     */
    default void postStart(T lifecycle) {
    }

    /**
     * Hook to perform actions before the lifecycle object is stopped.
     */
    default void preStop(T lifecycle) {
    }

    /**
     * Hook to perform actions after the lifecycle object has stopped.
     */
    default void postStop(T lifecycle) {
    }
}


