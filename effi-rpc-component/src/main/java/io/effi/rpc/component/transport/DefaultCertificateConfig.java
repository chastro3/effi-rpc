package io.effi.rpc.component.transport;

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
 * Provide the default implementation of {@link CertificateConfig}.
 */
public record DefaultCertificateConfig(String name, byte[] certChain, byte[] privateKey,
                                       String privateKeyPassword, byte[] trustCert, boolean clientAuthEnabled)
        implements CertificateConfig {

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

    /**
     * Builds {@link DefaultCertificateConfig} instance.
     */
    public static class Builder implements FluentBuilder<DefaultCertificateConfig, Builder> {

        private String name;

        private byte[] certChain;

        private byte[] privateKey;

        private String privateKeyPassword;

        private byte[] trustCert;

        private boolean clientAuthEnabled;

        public Builder name(String name) {
            this.name = name;
            return self();
        }

        public Builder certChainPath(String certChainPath) {
            if (certChainPath != null) {
                try {
                    this.certChain = readAllBytes(new FileInputStream(certChainPath), certChainPath);
                } catch (FileNotFoundException e) {
                    throw PredefinedErrorCode.READ_CERT_FILE.fail(e, certChainPath);
                }
            }
            return self();
        }

        public Builder certChain(InputStream certChain) {
            this.certChain = readAllBytes(certChain, "certChain InputStream");
            return self();
        }

        public Builder privateKeyPath(String privateKeyPath) {
            if (privateKeyPath != null) {
                try {
                    this.privateKey = readAllBytes(new FileInputStream(privateKeyPath), privateKeyPath);
                } catch (FileNotFoundException e) {
                    throw EffiRpcException.wrap(PredefinedErrorCode.READ_CERT_FILE, e, privateKeyPath);
                }
            }
            return self();
        }

        public Builder privateKey(InputStream privateKey) {
            this.privateKey = readAllBytes(privateKey, "privateKey InputStream");
            return self();
        }

        public Builder privateKeyPassword(String privateKeyPassword) {
            this.privateKeyPassword = privateKeyPassword;
            return self();
        }

        public Builder trustCertPath(String trustCertPath) {
            if (trustCertPath != null) {
                try {
                    this.trustCert = readAllBytes(new FileInputStream(trustCertPath), trustCertPath);
                } catch (FileNotFoundException e) {
                    throw PredefinedErrorCode.READ_CERT_FILE.fail(e, trustCertPath);
                }
            }
            return self();
        }

        public Builder trustCert(InputStream trustCert) {
            this.trustCert = readAllBytes(trustCert, "trustCert InputStream");
            return self();
        }

        public Builder clientAuthEnabled(boolean clientAuthEnabled) {
            this.clientAuthEnabled = clientAuthEnabled;
            return self();
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

