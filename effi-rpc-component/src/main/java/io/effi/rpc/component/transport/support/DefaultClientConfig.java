package io.effi.rpc.component.transport.support;

import io.effi.rpc.component.transport.ClientConfig;
import io.effi.rpc.component.transport.ProtocolStack;
import io.effi.rpc.config.Options;
import io.effi.rpc.constant.Constant;
import io.effi.rpc.util.Pair;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provide the default implementation of {@link ClientConfig}.
 */
public class DefaultClientConfig extends AbstractEndpointConfig implements ClientConfig {

    private static final Map<Pair<String, ProtocolStack>, DefaultClientConfig> DEFAULT_CONFIGS = new ConcurrentHashMap<>(4);

    public DefaultClientConfig(String protocol, ProtocolStack protocolStack, String id, Options options) {
        super(protocol, protocolStack, id, options);
    }

    /**
     * Returns the default client configuration for the specified protocol and stack.
     * Creates and caches a new instance if one doesn't exist.
     *
     * @param protocolName the protocol id
     * @param stack        the protocol stack
     * @return the default client configuration
     */
    public static DefaultClientConfig cached(String protocolName, ProtocolStack stack) {
        Pair<String, ProtocolStack> key = Pair.of(protocolName, stack);
        return DEFAULT_CONFIGS.computeIfAbsent(key, k ->
                new DefaultClientConfig(protocolName, stack, defaultId(protocolName), Options.empty())
        );
    }

    private static String defaultId(String protocolName) {
        return protocolName + "-" + Constant.DEFAULT_NAME;
    }
}

