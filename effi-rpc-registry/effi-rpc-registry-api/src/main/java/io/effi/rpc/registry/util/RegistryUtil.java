package io.effi.rpc.registry.util;

import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.registry.ServiceInstance;

import java.net.InetSocketAddress;

/**
 * Provides registry identifier and scope lookup helpers.
 */
public final class RegistryUtil {

    private RegistryUtil() {
    }

    /**
     * Generates an instance id from a protocol and socket address.
     *
     * @param protocol protocol name
     * @param address  socket address
     * @return generated instance id
     */
    public static String generateId(String protocol, InetSocketAddress address) {
        return generateId(protocol, address.getHostString(), address.getPort());
    }

    /**
     * Generates an instance id from a protocol, host, and port.
     *
     * @param protocol protocol name
     * @param host     host or IP address
     * @param port     listening port
     * @return generated instance id
     */
    public static String generateId(String protocol, String host, int port) {
        return "<" + protocol + ">" + host + ":" + port;
    }

    /**
     * Resolves the platform recorded in instance metadata.
     *
     * @param instance service instance
     * @return the resolved platform, or {@code null} when absent
     */
    public static ScopedPlatform lookupPlatform(ServiceInstance instance) {
        String platformName = instance.metadata().get(KeyConstant.PLATFORM);
        return ScopedPlatform.lookup(platformName);
    }

    /**
     * Resolves the application recorded in instance metadata.
     *
     * @param instance service instance
     * @return the resolved application, or {@code null} when absent
     */
    public static ScopedApplication lookupApplication(ServiceInstance instance) {
        ScopedPlatform platform = lookupPlatform(instance);
        if (platform == null) {
            return null;
        }
        String applicationName = instance.metadata().get(KeyConstant.APPLICATION);
        return platform.lookupApplication(applicationName);
    }

}
