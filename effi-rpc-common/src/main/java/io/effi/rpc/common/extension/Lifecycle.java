package io.effi.rpc.common.extension;

/**
 * Defines the lifecycle methods for a component:
 * initialization, startup, and shutdown.
 */
public interface Lifecycle {

    /**
     * Initializes the component, typically called upon creation.
     */
    default void init() {

    }

    /**
     * Starts the component, typically called when ready to run.
     */
    default void start() {

    }

    /**
     * Stops the component, typically called during shutdown.
     */
    default void stop() {

    }

    /**
     * Enum representing the lifecycle states of a component.
     */
    enum State {

        /**
         * The component has been initialized but not yet started.
         */
        INITIALIZED,

        /**
         * The component is actively running.
         */
        STARTED,

        /**
         * The component has been stopped.
         */
        STOPPED
    }

}
