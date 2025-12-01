package io.effi.rpc.component.transport.support;

import io.effi.rpc.component.transport.CertificateConfig;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.FileUtil;
import io.effi.rpc.trait.FluentBuilder;
import io.effi.rpc.util.StringUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 * Provide the default implementation of {@link CertificateConfig}.
 */
public class DefaultCertificateConfig implements CertificateConfig {

    private final String id;

    private final byte[] certChain;

    private final byte[] privateKey;

    private final String privateKeyPassword;

    private final byte[] trustCert;

    private final boolean clientAuthEnabled;

    private DefaultCertificateConfig(Builder builder) {
        this.id = AssertUtil.notBlank(builder.id, "id");
        this.certChain = AssertUtil.notNull(builder.certChain, "certChain");
        this.privateKey = AssertUtil.notNull(builder.privateKey, "privateKey");
        this.privateKeyPassword = StringUtil.isBlankOrDefault(builder.privateKeyPassword, null);
        this.trustCert = builder.trustCert;
        this.clientAuthEnabled = builder.clientAuthEnabled;
    }

    public static Builder builder() {
        return new Builder();
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

    @Override
    public String id() {
        return id;
    }

    /**
     * Builds {@link DefaultCertificateConfig} instance.
     */
    public static class Builder implements FluentBuilder<DefaultCertificateConfig, Builder> {

        private String id;

        private byte[] certChain;

        private byte[] privateKey;

        private String privateKeyPassword;

        private byte[] trustCert;

        private boolean clientAuthEnabled;

        public Builder id(String id) {
            this.id = id;
            return self();
        }

        public Builder certChainPath(String certChainPath) {
            this.certChain = readFileBytes(certChainPath);
            return self();
        }

        public Builder certChain(InputStream certChain) {
            this.certChain = readStreamBytes(certChain, "certChain");
            return self();
        }

        public Builder privateKeyPath(String privateKeyPath) {
            this.privateKey = readFileBytes(privateKeyPath);
            return self();
        }

        public Builder privateKey(InputStream privateKey) {
            this.privateKey = readStreamBytes(privateKey, "privateKey");
            return self();
        }

        public Builder privateKeyPassword(String privateKeyPassword) {
            this.privateKeyPassword = privateKeyPassword;
            return self();
        }

        public Builder trustCertPath(String trustCertPath) {
            this.trustCert = readFileBytes(trustCertPath);
            return self();
        }

        public Builder trustCert(InputStream trustCert) {
            this.trustCert = readStreamBytes(trustCert, "trustCert");
            return self();
        }

        public Builder clientAuthEnabled(boolean clientAuthEnabled) {
            this.clientAuthEnabled = clientAuthEnabled;
            return self();
        }

        @Override
        public DefaultCertificateConfig build() {
            return new DefaultCertificateConfig(this);
        }

        private byte[] readFileBytes(String path) {
            AssertUtil.notBlank(path, "path");
            try {
                return FileUtil.toBytes(new FileInputStream(path));
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to read bytes from '" + path + "'", e);
            }
        }

        private byte[] readStreamBytes(InputStream stream, String info) {
            AssertUtil.notNull(stream, "stream");
            try {
                return FileUtil.toBytes(stream);
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to read bytes from '" + info + "'", e);
            }
        }
    }

}

