package io.effi.rpc.boot;

import io.effi.rpc.annotation.spi.Extension;
import io.effi.rpc.base.ScheduledThreadPool;
import io.effi.rpc.base.Scheduler;
import io.effi.rpc.base.ServiceHost;
import io.effi.rpc.base.ThreadPool;
import io.effi.rpc.base.event.DisruptorEventDispatcher;
import io.effi.rpc.base.event.EventDispatcher;
import io.effi.rpc.base.filter.Filter;
import io.effi.rpc.component.ApplicationConfiguration;
import io.effi.rpc.component.EffiRpcApplication;
import io.effi.rpc.component.EffiRpcModule;
import io.effi.rpc.component.EffiRpcPlatform;
import io.effi.rpc.component.ModuleConfiguration;
import io.effi.rpc.component.PlatformConfiguration;
import io.effi.rpc.constant.Constant;
import io.effi.rpc.constant.Tags;
import io.effi.rpc.executor.RpcThreadPool;
import io.effi.rpc.metrics.event.CalleeMetricsEvent;
import io.effi.rpc.metrics.event.CalleeMetricsEventListener;
import io.effi.rpc.metrics.event.CallerMetricsEvent;
import io.effi.rpc.metrics.event.CallerMetricsEventListener;
import io.effi.rpc.metrics.filter.CalleeExecuteRecordFilter;
import io.effi.rpc.metrics.filter.CallerMetricsFilter;
import io.effi.rpc.transport.heartbeat.IdleEvent;
import io.effi.rpc.transport.heartbeat.IdleEventListener;
import io.effi.rpc.transport.heartbeat.RefreshIdleCountEvent;
import io.effi.rpc.transport.heartbeat.RefreshIdleCountEventListener;

import java.util.Collection;
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
    public static class PlatformInitializedConfiguration implements PlatformConfiguration {
        @Override
        public void preInit(EffiRpcPlatform platform) {
            platform.register(Scheduler.class, new ScheduledThreadPool(platform)).register(EventDispatcher.class, new DisruptorEventDispatcher(platform));
            EventDispatcher eventDispatcher = platform.lookup(EventDispatcher.class);
            eventDispatcher.registerListener(RefreshIdleCountEvent.class, new RefreshIdleCountEventListener());
            eventDispatcher.registerListener(IdleEvent.class, new IdleEventListener());
            eventDispatcher.registerListener(CallerMetricsEvent.class, new CallerMetricsEventListener());
            eventDispatcher.registerListener(CalleeMetricsEvent.class, new CalleeMetricsEventListener());
            platform.register(Filter.class, new CalleeExecuteRecordFilter(), Tags.FORCE_ACTIVE)
                    .register(Filter.class, new CallerMetricsFilter(), Tags.FORCE_ACTIVE);
            String serverHybrid = Constant.DEFAULT_SERVER_HYBRID_THREAD_POOL;
            ExecutorService serverHybridExecutor = RpcThreadPool.defaultIOExecutor(serverHybrid);
            String clientHybrid = Constant.DEFAULT_CLIENT_HYBRID_THREAD_POOL;
            ExecutorService clientHybridExecutor = RpcThreadPool.defaultCPUExecutor(clientHybrid);
            platform.register(ThreadPool.class, new ThreadPool(serverHybrid, serverHybridExecutor))
                    .register(ThreadPool.class, new ThreadPool(clientHybrid, clientHybridExecutor));
        }
    }

    /**
     * Starts the application.
     */
    @Extension(NAME)
    public static class ApplicationInitializedConfiguration implements ApplicationConfiguration {
        @Override
        public void postStart(EffiRpcApplication application) {
            Collection<ServiceHost> serviceHosts = application.platform().listOf(ServiceHost.class);
            for (ServiceHost serviceHost : serviceHosts) {
                if (!serviceHost.isActive()) {
                    serviceHost.start();
                }
            }
        }
    }

    /**
     * Initializes the module.Registers shared filters for the module's caller and callee metrics.
     */
    @Extension(NAME)
    public static class ModuleInitializedConfiguration implements ModuleConfiguration {

        @Override
        public void postInit(EffiRpcModule module) {

        }
    }

}
