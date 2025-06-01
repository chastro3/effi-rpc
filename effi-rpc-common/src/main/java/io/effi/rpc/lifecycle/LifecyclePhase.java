package io.effi.rpc.lifecycle;

import java.util.List;

/**
 * Defines lifecycle phases and their corresponding transitions.
 * Supports pre- and post-execution hooks for each phase.
 */
public enum LifecyclePhase {

    INIT(Lifecycle.State.INITIALIZED) {
        @Override
        public boolean isAllowed(Lifecycle.State state) {
            return state == null;
        }

        @Override
        <L extends Lifecycle, T extends LifecycleConfiguration<L>> void pre(List<T> configurations, L lifecycle) {
            configurations.forEach(configuration -> configuration.preInit(lifecycle));
        }


        @Override
        <L extends Lifecycle, T extends LifecycleConfiguration<L>> void post(List<T> configurations, L lifecycle) {
            configurations.forEach(configuration -> configuration.postInit(lifecycle));
        }
    },

    START(Lifecycle.State.STARTED) {
        @Override
        public boolean isAllowed(Lifecycle.State state) {
            return state == Lifecycle.State.INITIALIZED || state == Lifecycle.State.STOPPED;
        }

        @Override
        <L extends Lifecycle, T extends LifecycleConfiguration<L>> void pre(List<T> configurations, L lifecycle) {
            configurations.forEach(configuration -> configuration.preStart(lifecycle));
        }

        @Override
        <L extends Lifecycle, T extends LifecycleConfiguration<L>> void post(List<T> configurations, L lifecycle) {
            configurations.forEach(configuration -> configuration.postStart(lifecycle));
        }
    },

    STOP(Lifecycle.State.STOPPED) {
        @Override
        public boolean isAllowed(Lifecycle.State state) {
            return state == Lifecycle.State.STARTED;
        }

        @Override
        <L extends Lifecycle, T extends LifecycleConfiguration<L>> void pre(List<T> configurations, L lifecycle) {
            configurations.forEach(configuration -> configuration.preStop(lifecycle));
        }

        @Override
        <L extends Lifecycle, T extends LifecycleConfiguration<L>> void post(List<T> configurations, L lifecycle) {
            configurations.forEach(configuration -> configuration.postStop(lifecycle));
        }
    };

    final Lifecycle.State next;

    LifecyclePhase(Lifecycle.State next) {
        this.next = next;
    }

    public <L extends Lifecycle, T extends LifecycleConfiguration<L>> void execute(List<T> configurations, L lifecycle, Runnable action) {
        pre(configurations, lifecycle);
        action.run();
        post(configurations, lifecycle);
    }

    public abstract boolean isAllowed(Lifecycle.State state);


    public Lifecycle.State next() {
        return next;
    }

    abstract <L extends Lifecycle, T extends LifecycleConfiguration<L>> void pre(List<T> configurations, L lifecycle);


    abstract <L extends Lifecycle, T extends LifecycleConfiguration<L>> void post(List<T> configurations, L lifecycle);
}
