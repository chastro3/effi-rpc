package io.effi.rpc.boot;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.component.ComponentRegistry;
import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.event.DisruptorEventDispatcher;
import io.effi.rpc.component.event.EventDispatcher;
import io.effi.rpc.component.support.Scheduler;
import io.effi.rpc.component.support.ThreadPool;
import io.effi.rpc.config.ConfigNames;
import io.effi.rpc.context.metrics.event.CalleeMetricsEvent;
import io.effi.rpc.context.metrics.event.CalleeMetricsEventListener;
import io.effi.rpc.context.metrics.event.CallerMetricsEvent;
import io.effi.rpc.context.metrics.event.CallerMetricsEventListener;
import io.effi.rpc.executor.RpcThreadPool;
import io.effi.rpc.transport.idle.IdleEvent;
import io.effi.rpc.transport.idle.IdleEventListener;
import io.effi.rpc.transport.idle.RefreshIdleCountEvent;
import io.effi.rpc.transport.idle.RefreshIdleCountEventListener;

import java.util.concurrent.ExecutorService;

/**
 * Initializes configurations for the application and module,setting up event listeners and filters.
 */
public class InitializedConfiguration {

    private static final String NAME = "initializedConfiguration";

    /**
     * Initializes the application.Registers default event listeners for various events.
     */
    @Extension(NAME)
    public static class PlatformInitializedListener implements ScopedPlatform.Listener {
        @Override
        public void onInitializing(ScopedPlatform platform) {
            ComponentRegistry registry = platform.registry();
            registry.register(Scheduler.class, new Scheduler())
                    .register(EventDispatcher.class, new DisruptorEventDispatcher(platform));
            EventDispatcher eventDispatcher = platform.singleComponent(EventDispatcher.class);
            eventDispatcher.registerListener(RefreshIdleCountEvent.class, new RefreshIdleCountEventListener());
            eventDispatcher.registerListener(IdleEvent.class, new IdleEventListener());
            eventDispatcher.registerListener(CallerMetricsEvent.class, new CallerMetricsEventListener());
            eventDispatcher.registerListener(CalleeMetricsEvent.class, new CalleeMetricsEventListener());
            String serverHybrid = ConfigNames.CALLEE_THREAD_POOL.defaultValue();
            ExecutorService serverHybridExecutor = RpcThreadPool.defaultIOExecutor(serverHybrid);
            String clientHybrid = ConfigNames.THREAD_POOL.defaultValue();
            ExecutorService clientHybridExecutor = RpcThreadPool.defaultCPUExecutor(clientHybrid);
            registry.register(ThreadPool.class, new ThreadPool(serverHybrid, serverHybridExecutor))
                    .register(ThreadPool.class, new ThreadPool(clientHybrid, clientHybridExecutor));
        }


    }

    /**
     * Starts the application.
     */
    @Extension(NAME)
    public static class ApplicationInitializedListener implements ScopedApplication.Listener {
        @Override
        public void onStarted(ScopedApplication application) {
            ApplicationServiceRegistrar applicationServiceRegistrar = application.singleComponent(ApplicationServiceRegistrar.class);
            if (applicationServiceRegistrar == null) {
                applicationServiceRegistrar = new ApplicationServiceRegistrar(application);
            }
            applicationServiceRegistrar.register();
        }
    }
}
