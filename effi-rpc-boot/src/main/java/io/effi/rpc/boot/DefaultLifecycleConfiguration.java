package io.effi.rpc.boot;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.event.EventBus;
import io.effi.rpc.component.event.MpscEventBus;
import io.effi.rpc.component.metrics.DefaultMetrics;
import io.effi.rpc.component.tools.Scheduler;
import io.effi.rpc.concurrent.Deadline;
import io.effi.rpc.concurrent.Result;
import io.effi.rpc.context.CallFutureRegistry;
import io.effi.rpc.logging.Logger;
import io.effi.rpc.logging.LoggerFactory;
import io.effi.rpc.metrics.Metrics;
import io.effi.rpc.metrics.MetricsOptions;
import io.effi.rpc.metrics.report.LoggingMetricsReporter;
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
            Scheduler scheduler = new Scheduler();
            MpscEventBus eventBus = new MpscEventBus(platform);
            Metrics metrics = new DefaultMetrics(platform);
            CallFutureRegistry callFutureRegistry = new CallFutureRegistry();
            ChannelCallBindings channelCallBindings = new ChannelCallBindings(callFutureRegistry);
            metrics.register(eventBus.metrics());
            registerMetricsReporter(platform, metrics, scheduler);
            platform.registry()
                    .register(Scheduler.class, scheduler)
                    .register(EventBus.class, eventBus)
                    .register(Metrics.class, metrics)
                    .register(CallFutureRegistry.class, callFutureRegistry)
                    .register(ChannelCallBindings.class, channelCallBindings);
            registerDefaultEvents(eventBus);
            eventBus.start();
        }

        private void registerMetricsReporter(ScopedPlatform platform, Metrics metrics, Scheduler scheduler) {
            if (!platform.options().option(MetricsOptions.ENABLED)) {
                return;
            }
            metrics.registerReporter(new LoggingMetricsReporter());
            long reportIntervalMillis = platform.options().option(MetricsOptions.REPORT_INTERVAL_MILLIS);
            if (reportIntervalMillis > 0L) {
                scheduler.addPeriodic(
                        metrics::report,
                        reportIntervalMillis,
                        reportIntervalMillis,
                        TimeUnit.MILLISECONDS
                );
            }
        }

        private void registerDefaultEvents(EventBus eventBus) {
            eventBus.register(RefreshIdleCountEvent.class, new RefreshIdleCountEventHandler())
                    .register(IdleEvent.class, new IdleEventHandler());
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
