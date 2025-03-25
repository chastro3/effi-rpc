package io.effi.rpc.transport;

import io.effi.rpc.common.constant.Constant;
import io.effi.rpc.common.constant.KeyConstant;
import io.effi.rpc.common.exception.EffiRpcException;
import io.effi.rpc.common.exception.PredefinedErrorCode;
import io.effi.rpc.common.extension.collection.LazyMap;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.ssl.*;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * SslContext Factory.
 */
public class SslContextFactory {

    private static final Map<String, SslContext> SERVER_SSL_CONTEXT = new LazyMap<>(HashMap::new);

    private static final Map<String, SslContext> CLIENT_SSL_CONTEXT = new LazyMap<>(HashMap::new);

    private static final byte[] CA_BYTES;

    private static final byte[] SERVER_CERT_BYTES;

    private static final byte[] SERVER_KEY_BYTES;

    private static final byte[] CLIENT_CERT_BYTES;

    private static final byte[] CLIENT_KEY_BYTES;

    static {
        try {
            CA_BYTES = NettySupport.getSslBytes(KeyConstant.CA_PATH, Constant.INTERNAL_CERTS_PATH + "ca-cert.pem");
            SERVER_CERT_BYTES = NettySupport.getSslBytes(KeyConstant.SERVER_CERT_PATH, Constant.INTERNAL_CERTS_PATH + "server-cert.pem");
            SERVER_KEY_BYTES = NettySupport.getSslBytes(KeyConstant.SERVER_KEY_PATH, Constant.INTERNAL_CERTS_PATH + "server-pkcs8-key.pem");
            CLIENT_CERT_BYTES = NettySupport.getSslBytes(KeyConstant.CLIENT_CERT_PATH, Constant.INTERNAL_CERTS_PATH + "client-cert.pem");
            CLIENT_KEY_BYTES = NettySupport.getSslBytes(KeyConstant.CLIENT_KEY_PATH, Constant.INTERNAL_CERTS_PATH + "client-pkcs8-key.pem");
        } catch (IOException e) {
            throw EffiRpcException.wrap(PredefinedErrorCode.READ_CERT_RESOURCE, e);
        }
    }

    /**
     * Create ssl context for server.
     *
     * @param supportedProtocols
     * @return
     */
    public static SslContext acquireForServer(String... supportedProtocols) {
        return SERVER_SSL_CONTEXT.computeIfAbsent(sslContextKey(supportedProtocols), key -> {
            SslProvider provider = SslProvider.isAlpnSupported(SslProvider.OPENSSL) ? SslProvider.OPENSSL : SslProvider.JDK;
            //SelfSignedCertificate ssc = new SelfSignedCertificate();
            try {
                return SslContextBuilder.forServer(NettySupport.readBytes(SERVER_CERT_BYTES), NettySupport.readBytes(SERVER_KEY_BYTES))
                        .sslProvider(provider)
                        /* NOTE: the cipher filter may not include all ciphers required by the HTTP/2 specification.
                         * Please refer to the HTTP/2 specification for cipher requirements. */
                        .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                        .trustManager(NettySupport.readBytes(CA_BYTES))
                        .applicationProtocolConfig(
                                new ApplicationProtocolConfig(
                                        ApplicationProtocolConfig.Protocol.ALPN,
                                        // NO_ADVERTISE is currently the only mode supported by both OpenSsl and JDK providers.
                                        ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                                        // ACCEPT is currently the only mode supported by both OpenSsl and JDK providers.
                                        ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                                        supportedProtocols)
                        ).build();
            } catch (SSLException e) {
                throw EffiRpcException.wrap(PredefinedErrorCode.SSL, e);
            }
        });
    }

    /**
     * Create ssl context for client.
     *
     * @param supportedProtocols
     * @return
     */
    public static SslContext acquireForClient(String... supportedProtocols) {
        return CLIENT_SSL_CONTEXT.computeIfAbsent(sslContextKey(supportedProtocols), key -> {
            SslProvider provider = SslProvider.isAlpnSupported(SslProvider.OPENSSL) ? SslProvider.OPENSSL : SslProvider.JDK;
            try {
                return SslContextBuilder.forClient()
                        .sslProvider(provider)
                        .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                        .keyManager(NettySupport.readBytes(CLIENT_CERT_BYTES), NettySupport.readBytes(CLIENT_KEY_BYTES))
                        // you probably won't want to use this in production, but it is fine for this example:
                        .trustManager(NettySupport.readBytes(CA_BYTES))
                        .applicationProtocolConfig(new ApplicationProtocolConfig(
                                ApplicationProtocolConfig.Protocol.ALPN,
                                ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                                ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                                supportedProtocols))
                        .build();
            } catch (SSLException e) {
                throw EffiRpcException.wrap(PredefinedErrorCode.SSL, e);
            }
        });

    }

    private static String sslContextKey(String... supportedProtocols) {
        return String.join("-", supportedProtocols);
    }
}
