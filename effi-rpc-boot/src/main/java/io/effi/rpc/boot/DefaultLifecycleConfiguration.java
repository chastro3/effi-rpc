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
import io.effi.rpc.governance.metrics.GovernanceMetrics;
import io.effi.rpc.logging.Logger;
import io.effi.rpc.logging.LoggerFactory;
import io.effi.rpc.metrics.Metrics;
import io.effi.rpc.metrics.MetricsOptions;
import io.effi.rpc.transport.ChannelCallBindings;
import io.effi.rpc.transport.idle.IdleEvent;
import io.effi.rpc.transport.idle.IdleEventHandler;

import java.util.concurrent.TimeUnit;

/**
 * Provides default lifecycle configuration for the platform and application.
 */
public class DefaultLifecycleConfiguration {

    private static final String NAME = "defaultLifecycleConfiguration";

    private static final Logger logger = LoggerFactory.getLogger(DefaultLifecycleConfiguration.class);

    /**
     * Initializes the platform singletons and the default event handlers.
     */
    @Extension(NAME)
    public static class PlatformLifecycleListener implements ScopedPlatform.Listener {

        @Override
        public void onInitializing(ScopedPlatform platform) {
            Scheduler scheduler = new Scheduler();
            MpscEventBus eventBus = new MpscEventBus(platform);
            Metrics metrics = new DefaultMetrics(platform);
            GovernanceMetrics governanceMetrics = new GovernanceMetrics();
            CallFutureRegistry callFutureRegistry = new CallFutureRegistry();
            ChannelCallBindings channelCallBindings = new ChannelCallBindings(callFutureRegistry);
            platform.registry()
                    .register(Scheduler.class, scheduler)
                    .register(EventBus.class, eventBus)
                    .register(Metrics.class, metrics)
                    .register(GovernanceMetrics.class, governanceMetrics)
                    .register(CallFutureRegistry.class, callFutureRegistry)
                    .register(ChannelCallBindings.class, channelCallBindings);
            metrics.register(governanceMetrics);
            connectMetrics(platform, eventBus, metrics, scheduler);
            registerDefaultEvents(eventBus);
            eventBus.start();
        }

        private void connectMetrics(ScopedPlatform platform, MpscEventBus eventBus, Metrics metrics, Scheduler scheduler) {
            metrics.register(eventBus.metrics());
            if (!platform.options().option(MetricsOptions.ENABLED)) {
                return;
            }
            long reportIntervalMillis = platform.options().option(MetricsOptions.REPORT_INTERVAL_MILLIS);
            if (reportIntervalMillis > 0L) {
                scheduler.addPeriodic(metrics::report, reportIntervalMillis, reportIntervalMillis, TimeUnit.MILLISECONDS);
            }
        }

        private void registerDefaultEvents(EventBus eventBus) {
            eventBus.register(IdleEvent.class, new IdleEventHandler());
        }
    }

    /**
     * Starts the application.
     */
    @Extension(NAME)
    public static class ApplicationLifecycleListener implements ScopedApplication.Listener {
        @Override
        public void onStarted(ScopedApplication application) {
            ApplicationServiceRegistrar registrar =
                    application.singleComponent(ApplicationServiceRegistrar.class);
            if (registrar == null) {
                registrar = new ApplicationServiceRegistrar(application);
            }
            registrar.register().onComplete(result -> {
                if (result.failed()) {
                    logger.error("Failed to register application '{}'", result.cause(), application.name());
                    application.close();
                }
            });
        }

        @Override
        public void onClosing(ScopedApplication application) {
            ApplicationServiceRegistrar registrar =
                    application.singleComponent(ApplicationServiceRegistrar.class);
            if (registrar == null) {
                return;
            }
            try {
                Result<Void> result = registrar.deregister()
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
