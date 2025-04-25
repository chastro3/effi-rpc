package io.effi.rpc.constant;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Core framework class for Effi-RPC.
 * Provide utility methods for retrieving the framework version,JVM-related operations.
 */
public final class EffiRpcFramework {

    public static final String GROUP_ID = "io.github.chastro3";

    private static final String FRAMEWORK_VERSION;

    private static volatile boolean HAS_REGISTERED_SHUTDOWN_HOOK = false;

    static {
        try {
            String path = "META-INF/effi-rpc/version";
            InputStream stream = EffiRpcFramework.class.getClassLoader().getResourceAsStream(path);
            if (stream == null) {
                throw new IllegalStateException("Can't find " + path + " file");
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            FRAMEWORK_VERSION = reader.readLine();
        } catch (Exception e) {
            throw new IllegalStateException("Can't read version", e);
        }
    }

    private EffiRpcFramework() {
        // Prevent instantiation
    }

    /**
     * Gets the current version of the Effi-RPC framework.
     */
    public static String version() {
        return FRAMEWORK_VERSION;
    }

    /**
     * Registers a shutdown hook to be executed when the JVM shuts down.
     *
     * @param hook the shutdown logic to be executed.
     */
    public static void registerShutdownHook(Runnable hook) {
        if (!HAS_REGISTERED_SHUTDOWN_HOOK) {
            synchronized (EffiRpcFramework.class) {
                if (!HAS_REGISTERED_SHUTDOWN_HOOK) {
                    Runtime.getRuntime().addShutdownHook(new Thread(hook));
                    HAS_REGISTERED_SHUTDOWN_HOOK = true;
                }
            }
        }
    }
}

