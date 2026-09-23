package io.effi.rpc.component.transport.support;

import io.effi.rpc.component.transport.CertificateConfig;
import io.effi.rpc.component.transport.EndpointConfig;
import io.effi.rpc.component.transport.ProtocolStack;
import io.effi.rpc.component.transport.options.TcpOptions;
import io.effi.rpc.option.Options;

public abstract class TcpEndpointConfig extends AbstractEndpointConfig {

    protected TcpEndpointConfig(
            String protocol,
            String id,
            Options options,
            CertificateConfig certificateConfig
    ) {
        super(protocol, ProtocolStack.TCP, id, options, certificateConfig);
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
            addOption(TcpOptions.NO_DELAY, noDelay);
            return self();
        }

        /**
         * Enable or disable TCP keep-alive.
         */
        default SELF keepAlive(boolean keepAlive) {
            addOption(TcpOptions.KEEP_ALIVE, keepAlive);
            return self();
        }

        /**
         * Enables or disable SSL/TLS.
         */
        default SELF ssl(boolean ssl) {
            addOption(TcpOptions.SSL, ssl);
            return self();
        }

    }
}
