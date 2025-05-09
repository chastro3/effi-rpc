package io.effi.rpc.transport.netty;

import io.effi.rpc.contract.config.CertificateConfig;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.util.collection.LazyMap;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.ssl.*;

import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generates SSL contexts for both server and client sides.
 */
public class SslContextFactory {

    private static final Logger logger = LoggerFactory.getLogger(SslContextFactory.class);

    private static final SslProvider SSL_PROVIDER = findSslProvider();

    private static final Map<String, SslContext> SERVER_SSL_CONTEXT = new LazyMap<>(ConcurrentHashMap::new);

    private static final Map<String, SslContext> CLIENT_SSL_CONTEXT = new LazyMap<>(ConcurrentHashMap::new);

    /**
     * Builds server-side SSL context with specified protocols and certificate config.
     *
     * @param supportedProtocols supported protocol array
     * @param config             certificate configuration
     * @return server-side SSL context
     */
    public static SslContext getForServer(String[] supportedProtocols, CertificateConfig config) {
        String key = sslContextKey(supportedProtocols, config.name());
        return SERVER_SSL_CONTEXT.computeIfAbsent(key, k -> createSslContext(supportedProtocols, config, true));
    }

    /**
     * Builds client-side SSL context with specified protocols and certificate config.
     *
     * @param supportedProtocols supported protocol array
     * @param config             certificate configuration
     * @return client-side SSL context
     */
    public static SslContext getForClient(String[] supportedProtocols, CertificateConfig config) {
        String key = sslContextKey(supportedProtocols, config.name());
        return CLIENT_SSL_CONTEXT.computeIfAbsent(key, k -> createSslContext(supportedProtocols, config, false));
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

            if (trustCert != null) {
                builder.trustManager(trustCert);
            }
            if (isServer) handleClientAuth(builder, config, trustCert);

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

        } catch (Exception e) {
            throw PredefinedErrorCode.CREATE_SSL.fail(e, isServer ? "server" : "client");
        } finally {
            close(certChain, privateKey, trustCert);
        }
    }

    private static String sslContextKey(String[] supportedProtocols, String name) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < supportedProtocols.length; i++) {
            builder.append(supportedProtocols[i]);
            if (i < supportedProtocols.length - 1) {
                builder.append('-');
            }
        }
        if (name != null && !name.isEmpty()) {
            builder.append('-').append(name);
        }
        return builder.toString();
    }

    private static void close(InputStream... inputStreams) {
        if (inputStreams != null) {
            for (InputStream inputStream : inputStreams) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        logger.error("Failed to close input stream", e);
                    }
                }
            }
        }
    }

    private static InputStream newInputStream(byte[] bytes) {
        if (bytes == null) return null;
        return new ByteArrayInputStream(bytes);
    }

    private static void handleClientAuth(SslContextBuilder builder, CertificateConfig config, InputStream trustCert) {
        if (trustCert != null) {
            builder.clientAuth(config.clientAuthEnabled() ? ClientAuth.REQUIRE : ClientAuth.OPTIONAL);
        } else {
            builder.clientAuth(ClientAuth.NONE);
        }
    }

    private static SslProvider findSslProvider() {
        if (OpenSsl.isAvailable()) {
            logger.info("Using OPENSSL provider.");
            return SslProvider.OPENSSL;
        } else {
            logger.warn("OpenSSL not available: " + OpenSsl.unavailabilityCause());
        }
        if (checkJdkSslSupport()) {
            logger.info("Using JDK provider.");
            return SslProvider.JDK;
        }
        throw new IllegalStateException(
                "No valid TLS provider found. Please check your environment and dependencies.\n" +
                        "Suggestions:\n" +
                        " - Add netty-tcnative-boringssl-static for OpenSSL support\n" +
                        " - Ensure your JDK supports TLS and ALPN\n" +
                        " - Alternatively, consider using Conscrypt or Jetty ALPN/NPN extensions"
        );
    }

    private static boolean checkJdkSslSupport() {
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, null, null);
            return true;
        } catch (Exception e) {
            logger.warn("JDK SSLContext initialization failed: " + e.getMessage());
            return false;
        }
    }
}
