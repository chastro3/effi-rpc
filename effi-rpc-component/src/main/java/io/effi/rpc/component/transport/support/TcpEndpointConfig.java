package io.effi.rpc.component.transport.support;

import io.effi.rpc.component.transport.EndpointConfig;
import io.effi.rpc.component.transport.ProtocolStack;
import io.effi.rpc.config.OptionName;
import io.effi.rpc.config.Options;

public abstract class TcpEndpointConfig extends AbstractEndpointConfig {

    public static final OptionName<Boolean> NO_DELAY = OptionName.of("tcpNoDelay", true);

    public static final OptionName<Boolean> KEEP_ALIVE = OptionName.of("tcpKeepAlive", true);

    public static final OptionName<Boolean> SSL = OptionName.of("ssl", false);

    protected TcpEndpointConfig(String protocol, String id, Options options) {
        super(protocol, ProtocolStack.TCP, id, options);
    }

    public interface Configurator<SELF extends Configurator<SELF>>
            extends EndpointConfig.Configurator<SELF> {

        /**
         * Enable or disable TCP_NO_DELAY (Nagle's algorithm).
         * <p>
         * When {@code true}, disables Nagle to reduce latency by sending small packets immediately.<br>
         * When {@code false}, enables Nagle to reduce packet count, which may increase latency.
         */
        default SELF noDelay(boolean noDelay) {
            addOption(NO_DELAY, noDelay);
            return self();
        }

        /**
         * Enable or disable TCP keep-alive.
         */
        default SELF keepAlive(boolean keepAlive) {
            addOption(KEEP_ALIVE, keepAlive);
            return self();
        }

        /**
         * Enables or disable SSL/TLS.
         */
        default SELF ssl(boolean ssl) {
            addOption(SSL, ssl);
            return self();
        }

    }
}
