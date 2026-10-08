package io.effi.rpc.core.registry;

import com.sun.management.OperatingSystemMXBean;
import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.context.Servant;
import io.effi.rpc.core.ApplicationServiceRegistrar;
import io.effi.rpc.core.ServerLauncher;
import io.effi.rpc.transport.endpoint.ChannelTracker;

import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Provides a runtime metric snapshot for registry metadata.
 */
public class DefaultRegistryMetadata {

    private static final OperatingSystemMXBean OS_BEAN = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

    private double cpuUsage;

    private double memoryUsage;

    private long connections;

    private int services;

    private double loadAverage;

    public DefaultRegistryMetadata() {

    }

    public DefaultRegistryMetadata(ScopedApplication application) {
        cpuUsage = round(OS_BEAN.getCpuLoad());
        loadAverage = round(OS_BEAN.getSystemLoadAverage());
        long totalMemory = OS_BEAN.getTotalMemorySize();
        long freeMemory = OS_BEAN.getFreeMemorySize();
        long usedMemory = totalMemory - freeMemory;
        memoryUsage = round((double) usedMemory / totalMemory);
        int activeService = 0;
        for (ScopedModule module : application.modules()) {
            activeService += module.componentCount(Servant.class);
        }
        services = activeService;
        ApplicationServiceRegistrar registrar =
                application.singleComponent(ApplicationServiceRegistrar.class);
        AtomicInteger activeConnection = new AtomicInteger();
        for (ServerLauncher serverLauncher : registrar.serverLaunchers()) {
            serverLauncher.server()
                    .ifPresent(server -> {
                        if (server instanceof ChannelTracker channelTracker) {
                            activeConnection.addAndGet(channelTracker.size());
                        }
                    });
        }
        connections = activeConnection.get();
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    /**
     * Converts a metadata map into a registry metadata snapshot.
     *
     * @param map metadata values by key
     * @return metadata snapshot
     */
    public static DefaultRegistryMetadata valueOf(Map<String, String> map) {
        DefaultRegistryMetadata metadata = new DefaultRegistryMetadata();
        metadata.cpuUsage(Double.parseDouble(map.get("cpuUsage")));
        metadata.memoryUsage(Double.parseDouble(map.get("memoryUsage")));
        metadata.connections(Long.parseLong(map.get("connections")));
        metadata.services(Integer.parseInt(map.get("services")));
        metadata.loadAverage(Double.parseDouble(map.get("loadAverage")));
        return metadata;
    }

    /**
     * Sets the CPU usage.
     *
     * @param cpuUsage CPU usage ratio
     * @return this metadata snapshot
     */
    public DefaultRegistryMetadata cpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
        return this;
    }

    /**
     * Sets the memory usage ratio.
     *
     * @param memoryUsage memory usage ratio
     * @return this metadata snapshot
     */
    public DefaultRegistryMetadata memoryUsage(double memoryUsage) {
        this.memoryUsage = memoryUsage;
        return this;
    }

    /**
     * Sets the connection count.
     *
     * @param connections connection count
     * @return this metadata snapshot
     */
    public DefaultRegistryMetadata connections(long connections) {
        this.connections = connections;
        return this;
    }

    /**
     * Sets the servant count.
     *
     * @param services servant count
     * @return this metadata snapshot
     */
    public DefaultRegistryMetadata services(int services) {
        this.services = services;
        return this;
    }

    /**
     * Sets the system load average.
     *
     * @param loadAverage system load average
     * @return this metadata snapshot
     */
    public DefaultRegistryMetadata loadAverage(double loadAverage) {
        this.loadAverage = loadAverage;
        return this;
    }

    /**
     * Returns the current CPU usage.
     */
    public double cpuUsage() {
        return cpuUsage;
    }

    /**
     * Returns the current memory usage ratio.
     */
    public double memoryUsage() {
        return memoryUsage;
    }

    /**
     * Returns the current connection count.
     */
    public long connections() {
        return connections;
    }

    /**
     * Returns the current servant count.
     */
    public int services() {
        return services;
    }

    /**
     * Returns the current system load average.
     */
    public double loadAverage() {
        return loadAverage;
    }

    /**
     * Converts this snapshot into a metadata map.
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
