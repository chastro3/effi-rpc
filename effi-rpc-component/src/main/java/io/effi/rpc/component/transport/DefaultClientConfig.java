package io.effi.rpc.component.transport;

import io.effi.rpc.config.Config;
import io.effi.rpc.config.ConfigValues;
import io.effi.rpc.config.DefaultConfig;
import io.effi.rpc.util.Pair;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provide the default implementation of {@link ClientConfig}.
 */
public class DefaultClientConfig extends AbstractEndpointConfig implements ClientConfig {

    private static final Map<Pair<String, ProtocolStack>, DefaultClientConfig> DEFAULT_CONFIGS = new ConcurrentHashMap<>(4);

    public DefaultClientConfig(String id, Config config, String protocol,
                               ProtocolStack protocolStack, CertificateConfig certificateConfig) {
        super(id, config, protocol, protocolStack, certificateConfig);
    }

    /**
     * Returns the default client configuration for the specified protocol and stack.
     * Creates and caches a new instance if one doesn't exist.
     *
     * @param protocolName the protocol name
     * @param stack        the protocol stack
     * @return the default client configuration
     */
    public static DefaultClientConfig fetch(String protocolName, ProtocolStack stack) {
        Pair<String, ProtocolStack> key = Pair.of(protocolName, stack);
        return DEFAULT_CONFIGS.computeIfAbsent(key,
                k -> new DefaultClientConfig(
                        defaultId(protocolName), DefaultConfig.empty(), protocolName, stack, null
                )
        );
    }

    private static String defaultId(String protocolName) {
        return protocolName + "-" + ConfigValues.DEFAULT;
    }
}

