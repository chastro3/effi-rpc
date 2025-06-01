package io.effi.rpc.boot.registry;

import com.sun.management.OperatingSystemMXBean;
import io.effi.rpc.base.Callee;
import io.effi.rpc.base.ServiceHost;
import io.effi.rpc.component.EffiRpcApplication;
import io.effi.rpc.component.EffiRpcModule;
import io.effi.rpc.boot.DefaultServiceHost;
import io.effi.rpc.transport.endpoint.Server;

import java.lang.management.ManagementFactory;
import java.util.Map;

/**
 * SystemInfo.
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

    public DefaultRegistryMetaData(ServiceHost serviceHost) {
        // Get the server CPU usage
        cpuUsage = round(OS_BEAN.getCpuLoad());
        loadAverage = round(OS_BEAN.getSystemLoadAverage());
        // Get the server memory usage
        long totalMemory = OS_BEAN.getTotalMemorySize();
        long freeMemory = OS_BEAN.getFreeMemorySize();
        long usedMemory = totalMemory - freeMemory;
        memoryUsage = round((double) usedMemory / totalMemory);
        int activeService = 0;
        if (serviceHost != null) {
            for (EffiRpcApplication application : serviceHost.platform().applications()) {
                for (EffiRpcModule module : application.modules()) {
                    activeService += module.sizeOf(Callee.class);
                }
            }
            services = activeService;
            if (serviceHost instanceof DefaultServiceHost defaultServerExporter) {
                    Server server = defaultServerExporter.server();
                    if (server != null) {
                        connections = server.channels().size();
                    }
                }
        }
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
