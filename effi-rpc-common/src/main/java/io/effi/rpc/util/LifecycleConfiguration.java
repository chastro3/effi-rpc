package io.effi.rpc.util;

/**
 * Manages the object lifecycle with hooks for each phase.
 */
public interface LifecycleConfiguration<T extends Lifecycle> {

    /**
     * Performs actions before initialization.
     */
    default void preInit(T lifecycle) {
    }

    /**
     * Performs actions after initialization.
     */
    default void postInit(T lifecycle) {
    }

    /**
     * Performs actions before start.
     */
    default void preStart(T lifecycle) {
    }

    /**
     * Performs actions after start.
     */
    default void postStart(T lifecycle) {
    }

    /**
     * Performs actions before stop.
     */
    default void preStop(T lifecycle) {
    }

    /**
     * Performs actions after stop.
     */
    default void postStop(T lifecycle) {
    }
}



