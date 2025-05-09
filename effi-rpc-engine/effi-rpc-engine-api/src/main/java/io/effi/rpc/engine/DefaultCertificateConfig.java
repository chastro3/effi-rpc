package io.effi.rpc.engine;

import io.effi.rpc.contract.config.CertificateConfig;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.FluentBuilder;
import io.effi.rpc.util.StringUtil;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Default implementation of {@link CertificateConfig}.
 */
public class DefaultCertificateConfig implements CertificateConfig {

    private final String name;

    private final byte[] certChain;

    private final byte[] privateKey;

    private final String privateKeyPassword;

    private final byte[] trustCert;

    private final boolean clientAuthEnabled;

    public DefaultCertificateConfig(String name, byte[] certChain, byte[] privateKey, String privateKeyPassword, byte[] trustCert, boolean clientAuthEnabled) {
        this.name = AssertUtil.notBlank(name, "name");
        this.certChain = AssertUtil.notNull(certChain, "certChain");
        this.privateKey = AssertUtil.notNull(privateKey, "privateKey");
        this.privateKeyPassword = StringUtil.isBlankOrDefault(privateKeyPassword, null);
        this.trustCert = trustCert;
        this.clientAuthEnabled = clientAuthEnabled;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public byte[] certChain() {
        return certChain;
    }

    @Override
    public byte[] privateKey() {
        return privateKey;
    }

    @Override
    public String privateKeyPassword() {
        return privateKeyPassword;
    }

    @Override
    public byte[] trustCert() {
        return trustCert;
    }

    @Override
    public boolean clientAuthEnabled() {
        return clientAuthEnabled;
    }

    /**
     * Builds {@link DefaultCertificateConfig} instances.
     */
    public static class Builder implements FluentBuilder<DefaultCertificateConfig, Builder> {

        private String name;

        private byte[] certChain;

        private byte[] privateKey;

        private String privateKeyPassword;

        private byte[] trustCert;

        private boolean clientAuthEnabled;

        /**
         * Sets the name.
         */
        public Builder name(String name) {
            this.name = name;
            return returnThis();
        }

        /**
         * Loads the certificate chain from the given file path.
         */
        public Builder certChainPath(String certChainPath) {
            if (certChainPath != null) {
                try {
                    this.certChain = readAllBytes(new FileInputStream(certChainPath), certChainPath);
                } catch (FileNotFoundException e) {
                    throw PredefinedErrorCode.READ_CERT_FILE.fail(e, certChainPath);
                }
            }
            return returnThis();
        }

        /**
         * Sets the certificate chain.
         */
        public Builder certChain(InputStream certChain) {
            this.certChain = readAllBytes(certChain, "certChain InputStream");
            return returnThis();
        }

        /**
         * Loads the private key from the given file path.
         */
        public Builder privateKeyPath(String privateKeyPath) {
            if (privateKeyPath != null) {
                try {
                    this.privateKey = readAllBytes(new FileInputStream(privateKeyPath), privateKeyPath);
                } catch (FileNotFoundException e) {
                    throw EffiRpcException.wrap(PredefinedErrorCode.READ_CERT_FILE, e, privateKeyPath);
                }
            }
            return returnThis();
        }

        /**
         * Sets the private key.
         */

        public Builder privateKey(InputStream privateKey) {
            this.privateKey = readAllBytes(privateKey, "privateKey InputStream");
            return returnThis();
        }

        /**
         * Sets the password for the private key.
         */
        public Builder privateKeyPassword(String privateKeyPassword) {
            this.privateKeyPassword = privateKeyPassword;
            return returnThis();
        }

        /**
         * Loads the trusted CA certificate from the given file path.
         */
        public Builder trustCertPath(String trustCertPath) {
            if (trustCertPath != null) {
                try {
                    this.trustCert = readAllBytes(new FileInputStream(trustCertPath), trustCertPath);
                } catch (FileNotFoundException e) {
                    throw PredefinedErrorCode.READ_CERT_FILE.fail(e, trustCertPath);
                }
            }
            return returnThis();
        }

        /**
         * Sets the trusted CA certificate.
         */
        public Builder trustCert(InputStream trustCert) {
            this.trustCert = readAllBytes(trustCert, "trustCert InputStream");
            return returnThis();
        }

        /**
         * Enables or disables client authentication.
         */
        public Builder clientAuthEnabled(boolean clientAuthEnabled) {
            this.clientAuthEnabled = clientAuthEnabled;
            return returnThis();
        }

        @Override
        public DefaultCertificateConfig build() {
            return new DefaultCertificateConfig(name, certChain, privateKey, privateKeyPassword, trustCert, clientAuthEnabled);
        }

        private byte[] readAllBytes(InputStream stream, String info) {
            AssertUtil.notNull(stream, "stream");
            try (stream; ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = stream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                return outputStream.toByteArray();
            } catch (Exception e) {
                throw PredefinedErrorCode.READ_CERT_FILE.fail(e, info);
            }
        }
    }

}

