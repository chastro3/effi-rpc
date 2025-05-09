package io.effi.rpc.engine;

import io.effi.rpc.constant.Constant;
import io.effi.rpc.contract.ThreadPool;
import io.effi.rpc.contract.module.ApplicationConfiguration;
import io.effi.rpc.contract.module.EffRpcApplication;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.contract.module.ModuleConfiguration;
import io.effi.rpc.event.EventDispatcher;
import io.effi.rpc.executor.RpcThreadPool;
import io.effi.rpc.metrics.event.CalleeMetricsEvent;
import io.effi.rpc.metrics.event.CalleeMetricsEventListener;
import io.effi.rpc.metrics.event.CallerMetricsEvent;
import io.effi.rpc.metrics.event.CallerMetricsEventListener;
import io.effi.rpc.metrics.filter.CalleeExecuteRecordFilter;
import io.effi.rpc.metrics.filter.CallerMetricsFilter;
import io.effi.rpc.spi.Extension;
import io.effi.rpc.transport.heartbeat.IdleEvent;
import io.effi.rpc.transport.heartbeat.IdleEventListener;
import io.effi.rpc.transport.heartbeat.RefreshIdleCountEvent;
import io.effi.rpc.transport.heartbeat.RefreshIdleCountEventListener;

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
    public static class ApplicationInitializedConfiguration implements ApplicationConfiguration {
        @Override
        public void postInit(EffRpcApplication application) {
            // register default event listeners
            EventDispatcher eventDispatcher = application.eventDispatcher();
            eventDispatcher.registerListener(RefreshIdleCountEvent.class, new RefreshIdleCountEventListener());
            eventDispatcher.registerListener(IdleEvent.class, new IdleEventListener());
            eventDispatcher.registerListener(CallerMetricsEvent.class, new CallerMetricsEventListener());
            eventDispatcher.registerListener(CalleeMetricsEvent.class, new CalleeMetricsEventListener());
        }
    }

    /**
     * Initializes the module.Registers shared filters for the module's caller and callee metrics.
     */
    @Extension(NAME)
    public static class ModuleInitializedConfiguration implements ModuleConfiguration {
        @Override
        public void postInit(EffiRpcModule module) {
            module.registerShared(new CalleeExecuteRecordFilter(), new CallerMetricsFilter(module));
            String serverHybrid = Constant.DEFAULT_SERVER_HYBRID_THREAD_POOL;
            ExecutorService serverHybridExecutor = RpcThreadPool.defaultIOExecutor(serverHybrid);
            module.register(new ThreadPool(serverHybrid, serverHybridExecutor));
            String clientHybrid = Constant.DEFAULT_CLIENT_HYBRID_THREAD_POOL;
            ExecutorService clientHybridExecutor = RpcThreadPool.defaultCPUExecutor(clientHybrid);
            module.register(new ThreadPool(serverHybrid, clientHybridExecutor));
        }
    }

}
