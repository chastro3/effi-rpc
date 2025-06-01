package io.effi.rpc.lifecycle;

/**
 * Defines lifecycle methods: initialization, startup, and shutdown.
 */
public interface Lifecycle {

    /**
     * Initializes the component.
     */
    default void init() {

    }

    /**
     * Starts the component.
     */
    default void start() {

    }

    /**
     * Stops the component.
     */
    default void stop() {

    }

    /**
     * Lifecycle states of a component.
     */
    enum State {

        /**
         * Initialized, not yet started.
         */
        INITIALIZED,

        /**
         * Actively running.
         */
        STARTED,

        /**
         * Stopped.
         */
        STOPPED
    }
}

