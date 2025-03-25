package io.effi.rpc.common.extension;

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
     *
     * @param lifecycle the lifecycle object being initialized
     */
    default void preInit(T lifecycle) {
    }

    /**
     * Hook to perform actions after the lifecycle object is initialized.
     *
     * @param lifecycle the lifecycle object that has been initialized
     */
    default void postInit(T lifecycle) {
    }

    /**
     * Hook to perform actions before the lifecycle object is started.
     *
     * @param lifecycle the lifecycle object being started
     */
    default void preStart(T lifecycle) {
    }

    /**
     * Hook to perform actions after the lifecycle object has started.
     *
     * @param lifecycle the lifecycle object that has been started
     */
    default void postStart(T lifecycle) {
    }

    /**
     * Hook to perform actions before the lifecycle object is stopped.
     *
     * @param lifecycle the lifecycle object being stopped
     */
    default void preStop(T lifecycle) {
    }

    /**
     * Hook to perform actions after the lifecycle object has stopped.
     *
     * @param lifecycle the lifecycle object that has been stopped
     */
    default void postStop(T lifecycle) {
    }
}


