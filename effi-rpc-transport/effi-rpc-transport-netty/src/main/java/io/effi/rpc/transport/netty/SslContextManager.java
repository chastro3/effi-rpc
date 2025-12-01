package io.effi.rpc.transport.netty;

import io.effi.rpc.component.transport.CertificateConfig;
import io.effi.rpc.component.transport.ClientConfig;
import io.effi.rpc.component.transport.EndpointConfig;
import io.effi.rpc.component.transport.ServerConfig;
import io.effi.rpc.component.transport.support.TcpEndpointConfig;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.util.ArrayIdentifier;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.Pair;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages SSL contexts for both server and client sides.
 * <p>
 * Provides SSL context management functionality for creating and caching
 * SSL contexts based on certificate configurations and supported protocols.
 */
public class SslContextManager {

    private static final Logger logger = LoggerFactory.getLogger(SslContextManager.class);

    private static final SslProvider SSL_PROVIDER = ensureSslProvider();

    private static final Map<Pair<ArrayIdentifier<String>, String>, SslContext> SERVER_SSL_CONTEXT = new ConcurrentHashMap<>(2);

    private static final Map<Pair<ArrayIdentifier<String>, String>, SslContext> CLIENT_SSL_CONTEXT = new ConcurrentHashMap<>(2);

    /**
     * Returns a server-side SSL context, creating it if necessary,
     * using specified protocols and configuration.
     */
    public static SslContext contextOf(String[] supportedProtocols, ServerConfig config) {
        return doContextOf(supportedProtocols, config);
    }

    /**
     * Returns a client-side SSL context, creating it if necessary,
     * using specified protocols and configuration.
     */
    public static SslContext contextOf(String[] supportedProtocols, ClientConfig config) {
        return doContextOf(supportedProtocols, config);
    }

    private static SslContext doContextOf(String[] supportedProtocols, EndpointConfig config) {
        if (!sslEnabled(config)) return null;
        boolean isServer = config instanceof ServerConfig;
        Pair<ArrayIdentifier<String>, String> key = generateSslContextKey(supportedProtocols, config.id());
        Map<Pair<ArrayIdentifier<String>, String>, SslContext> contextMap = isServer ? SERVER_SSL_CONTEXT : CLIENT_SSL_CONTEXT;
        return contextMap.computeIfAbsent(key, k -> createSslContext(supportedProtocols, config.certificateConfig(), isServer));
    }

    private static SslContext createSslContext(String[] supportedProtocols, CertificateConfig config, boolean isServer) {
        InputStream certChain = newInputStream(config.certChain());
        InputStream privateKey = newInputStream(config.privateKey());
        InputStream trustCert = newInputStream(config.trustCert());
        String keyPassword = config.privateKeyPassword();
        try {
            SslContextBuilder builder = isServer
                    ? SslContextBuilder.forServer(certChain, privateKey, keyPassword)
                    : SslContextBuilder.forClient().keyManager(certChain, privateKey, keyPassword);
            if (trustCert != null) builder.trustManager(trustCert);
            if (isServer) configureClientAuth(builder, config, trustCert);
            return builder.sslProvider(SSL_PROVIDER)
                    /* NOTE: the cipher filter may not include all ciphers required by the HTTP/2 specification.
                     * Please refer to the HTTP/2 specification for cipher requirements. */
                    .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                    .applicationProtocolConfig(
                            new ApplicationProtocolConfig(
                                    ApplicationProtocolConfig.Protocol.ALPN,
                                    // NO_ADVERTISE is currently the only mode supported by both OpenSsl and JDK providers.
                                    ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                                    // ACCEPT is currently the only mode supported by both OpenSsl and JDK providers.
                                    ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                                    supportedProtocols)
                    ).build();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create SSL context for '" + (isServer ? "server" : "client") + "'.", e);
        } finally {
            close(certChain, privateKey, trustCert);
        }
    }

    private static SslProvider ensureSslProvider() {
        if (OpenSsl.isAvailable()) {
            logger.info("Using OPENSSL provider.");
            return SslProvider.OPENSSL;
        } else {
            logger.info("Using JDK provider.");
            return SslProvider.JDK;
        }
    }

    private static boolean sslEnabled(EndpointConfig config) {
        return config.option(TcpEndpointConfig.SSL);
    }

    private static Pair<ArrayIdentifier<String>, String> generateSslContextKey(String[] supportedProtocols, String name) {
        ArrayIdentifier<String> identifier = ArrayIdentifier.of(supportedProtocols);
        return Pair.of(identifier, name);
    }

    private static InputStream newInputStream(byte[] bytes) {
        if (bytes == null) return null;
        return new ByteArrayInputStream(bytes);
    }

    private static void configureClientAuth(SslContextBuilder builder, CertificateConfig config, InputStream trustCert) {
        if (trustCert != null) {
            builder.clientAuth(config.clientAuthEnabled() ? ClientAuth.REQUIRE : ClientAuth.OPTIONAL);
        } else {
            builder.clientAuth(ClientAuth.NONE);
        }
    }

    private static void close(InputStream... inputStreams) {
        if (CollectionUtil.isNotEmpty(inputStreams)) {
            for (InputStream inputStream : inputStreams) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        logger.error("Failed to close input stream.", e);
                    }
                }
            }
        }
    }
}
