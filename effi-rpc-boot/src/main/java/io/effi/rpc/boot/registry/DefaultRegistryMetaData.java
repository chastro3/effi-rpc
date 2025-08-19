package io.effi.rpc.boot.registry;

import com.sun.management.OperatingSystemMXBean;
import io.effi.rpc.boot.ApplicationServiceRegistrar;
import io.effi.rpc.boot.ServerLauncher;
import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.context.Callee;
import io.effi.rpc.transport.endpoint.ChannelTracker;

import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SystemInfo.  todo 待优化
 */
public class DefaultRegistryMetaData {

    private static final OperatingSystemMXBean OS_BEAN = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

    private double cpuUsage;

    private double memoryUsage;

    private long connections;

    private int services;

    private double loadAverage;

    public DefaultRegistryMetaData() {

    }

    public DefaultRegistryMetaData(ScopedApplication application) {
        // Get the server CPU usage
        cpuUsage = round(OS_BEAN.getCpuLoad());
        loadAverage = round(OS_BEAN.getSystemLoadAverage());
        // Get the server memory usage
        long totalMemory = OS_BEAN.getTotalMemorySize();
        long freeMemory = OS_BEAN.getFreeMemorySize();
        long usedMemory = totalMemory - freeMemory;
        memoryUsage = round((double) usedMemory / totalMemory);
        int activeService = 0;
        for (ScopedModule module : application.modules()) {
            activeService += module.componentCount(Callee.class);
        }
        services = activeService;
        ApplicationServiceRegistrar serviceRegistrar = application.singleComponent(ApplicationServiceRegistrar.class);
        AtomicInteger activeConnection = new AtomicInteger();
        for (ServerLauncher serverLauncher : serviceRegistrar.serverLaunchers()) {
            serverLauncher.server()
                    .ifPresent(server -> {
                        if (server instanceof ChannelTracker channelTracker) {
                            activeConnection.addAndGet(channelTracker.size());
                        }
                    });
        }
        connections = activeConnection.get();
    }

    /**
     * Converts map to DefaultRegistryMetaData.
     */
    public static DefaultRegistryMetaData valueOf(Map<String, String> map) {
        DefaultRegistryMetaData defaultRegistryMetaData = new DefaultRegistryMetaData();
        defaultRegistryMetaData.cpuUsage(Double.parseDouble(map.get("cpuUsage")));
        defaultRegistryMetaData.memoryUsage(Double.parseDouble(map.get("memoryUsage")));
        defaultRegistryMetaData.connections(Long.parseLong(map.get("connections")));
        defaultRegistryMetaData.services(Integer.parseInt(map.get("services")));
        defaultRegistryMetaData.loadAverage(Double.parseDouble(map.get("loadAverage")));
        return defaultRegistryMetaData;
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    /**
     * Returns the cpuUsage.
     */
    public double cpuUsage() {
        return cpuUsage;
    }

    /**
     * Sets the cpuUsage.
     */
    public DefaultRegistryMetaData cpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
        return this;
    }

    /**
     * Returns the memoryUsage.
     */
    public double memoryUsage() {
        return memoryUsage;
    }

    /**
     * Sets the memoryUsage.
     */
    public DefaultRegistryMetaData memoryUsage(double memoryUsage) {
        this.memoryUsage = memoryUsage;
        return this;
    }

    /**
     * Returns the connections.
     */
    public long connections() {
        return connections;
    }

    /**
     * Sets the connections.
     */
    public DefaultRegistryMetaData connections(long connections) {
        this.connections = connections;
        return this;
    }

    /**
     * Returns the services.
     */
    public int services() {
        return services;
    }

    /**
     * Sets the services.
     */
    public DefaultRegistryMetaData services(int services) {
        this.services = services;
        return this;
    }

    /**
     * Returns the loadAverage.
     */
    public double loadAverage() {
        return loadAverage;
    }

    /**
     * Sets the loadAverage.
     */
    public DefaultRegistryMetaData loadAverage(double loadAverage) {
        this.loadAverage = loadAverage;
        return this;
    }

    /**
     * SystemInfo to map.
     */
    public Map<String, String> toMap() {
        return Map.of(
                "cpuUsage", String.valueOf(cpuUsage),
                "memoryUsage", String.valueOf(memoryUsage),
                "connections", String.valueOf(connections),
                "services", String.valueOf(services),
                "loadAverage", String.valueOf(loadAverage)
        );
    }
}
