package io.effi.rpc.registry.util;

import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.registry.ServiceInstance;

import java.net.InetSocketAddress;

public class RegistryUtil {


    public static String generateId(String protocol, InetSocketAddress address) {
        return generateId(protocol, address.getHostString(), address.getPort());
    }

    public static String generateId(String protocol, String host, int port) {
        return "<" + protocol + ">" + host + ":" + port;
    }

    public static ScopedPlatform lookupPlatform(ServiceInstance instance) {
        String platformName = instance.metadata().get(KeyConstant.PLATFORM);
        return ScopedPlatform.lookup(platformName);
    }

    public static ScopedApplication lookupApplication(ServiceInstance instance) {
        ScopedPlatform platform = lookupPlatform(instance);
        if (platform == null) return null;
        String applicationName = instance.metadata().get(KeyConstant.APPLICATION);
        return platform.lookupApplication(applicationName);
    }

}
