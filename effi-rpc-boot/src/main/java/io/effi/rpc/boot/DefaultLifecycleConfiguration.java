package io.effi.rpc.boot;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.component.ComponentRegistry;
import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.event.EventBus;
import io.effi.rpc.component.event.MpscEventBus;
import io.effi.rpc.component.tools.Scheduler;
import io.effi.rpc.concurrent.Deadline;
import io.effi.rpc.concurrent.Result;
import io.effi.rpc.context.CallFutureRegistry;
import io.effi.rpc.context.metrics.event.CalleeMetricsEvent;
import io.effi.rpc.context.metrics.event.CalleeMetricsEventHandler;
import io.effi.rpc.context.metrics.event.CallerMetricsEvent;
import io.effi.rpc.context.metrics.event.CallerMetricsEventHandler;
import io.effi.rpc.logging.Logger;
import io.effi.rpc.logging.LoggerFactory;
import io.effi.rpc.transport.ChannelCallBindings;
import io.effi.rpc.transport.idle.IdleEvent;
import io.effi.rpc.transport.idle.IdleEventHandler;
import io.effi.rpc.transport.idle.RefreshIdleCountEvent;
import io.effi.rpc.transport.idle.RefreshIdleCountEventHandler;

import java.util.concurrent.TimeUnit;

/**
 * Initializes configurations for the application and module,setting up event listeners and filters.
 */
public class DefaultLifecycleConfiguration {

    private static final String NAME = "defaultLifecycleConfiguration";

    private static final Logger logger = LoggerFactory.getLogger(DefaultLifecycleConfiguration.class);

    /**
     * Initializes the application.Registers default event listeners for various events.
     */
    @Extension(NAME)
    public static class PlatformLifecycleListener implements ScopedPlatform.Listener {
        @Override
        public void onInitializing(ScopedPlatform platform) {
            ComponentRegistry registry = platform.registry();
            CallFutureRegistry callFutureRegistry = new CallFutureRegistry();
            MpscEventBus eventBus = new MpscEventBus(platform);
            registry.register(Scheduler.class, new Scheduler())
                    .register(EventBus.class, eventBus)
                    .register(CallFutureRegistry.class, callFutureRegistry)
                    .register(ChannelCallBindings.class, new ChannelCallBindings(callFutureRegistry));
            eventBus.register(RefreshIdleCountEvent.class, new RefreshIdleCountEventHandler())
                    .register(IdleEvent.class, new IdleEventHandler())
                    .register(CallerMetricsEvent.class, new CallerMetricsEventHandler())
                    .register(CalleeMetricsEvent.class, new CalleeMetricsEventHandler());
            eventBus.start();
        }

    }

    /**
     * Starts the application.
     */
    @Extension(NAME)
    public static class ApplicationLifecycleListener implements ScopedApplication.Listener {
        @Override
        public void onStarted(ScopedApplication application) {
            ApplicationServiceRegistrar coordinator =
                    application.singleComponent(ApplicationServiceRegistrar.class);
            if (coordinator == null) {
                coordinator = new ApplicationServiceRegistrar(application);
            }
            coordinator.register();
        }

        @Override
        public void onClosing(ScopedApplication application) {
            ApplicationServiceRegistrar coordinator =
                    application.singleComponent(ApplicationServiceRegistrar.class);
            if (coordinator == null) {
                return;
            }
            try {
                Result<Void> result = coordinator.deregister()
                        .await(Deadline.after(30, TimeUnit.SECONDS));
                if (result.failed()) {
                    logger.error("Failed to stop application '{}'", result.cause(), application.name());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Interrupted while stopping application '{}'", e, application.name());
            }
        }
    }
}
