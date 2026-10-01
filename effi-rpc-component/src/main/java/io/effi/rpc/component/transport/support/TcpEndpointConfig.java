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
         * Enables or disables {@code TCP_NODELAY}.
         * <p>
         * When {@code true}, Nagle's algorithm is disabled to reduce latency.
         *
         * @param noDelay whether to disable Nagle's algorithm
         * @return this configurator
         */
        default SELF noDelay(boolean noDelay) {
            addOption(TcpOptions.NO_DELAY, noDelay);
            return self();
        }

        /**
         * Enables or disables TCP keep-alive.
         *
         * @param keepAlive whether TCP keep-alive is enabled
         * @return this configurator
         */
        default SELF keepAlive(boolean keepAlive) {
            addOption(TcpOptions.KEEP_ALIVE, keepAlive);
            return self();
        }

        /**
         * Enables or disables SSL/TLS.
         *
         * @param ssl whether SSL/TLS is enabled
         * @return this configurator
         */
        default SELF ssl(boolean ssl) {
            addOption(TcpOptions.SSL, ssl);
            return self();
        }

    }
}
