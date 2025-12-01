package io.effi.rpc.component.transport;

import io.effi.rpc.config.OptionName;
import io.effi.rpc.trait.Identifiable;

/**
 * Defines configurations for certificates used in secure connections.
 * <p>
 * Provides a standardized interface for managing certificate chains,
 * private keys, and trust certificates for TLS/SSL connections.
 */
public interface CertificateConfig  extends Identifiable {

    OptionName<CertificateConfig> NAME = OptionName.of("certificate");

    /**
     * Returns the byte array of the certificate chain.
     */
    byte[] certChain();

    /**
     * Returns the byte array of the private key.
     */
    byte[] privateKey();

    /**
     * Returns the password for the private key.
     */
    String privateKeyPassword();

    /**
     * Returns the byte array of the trusted certificate file.
     */
    byte[] trustCert();

    /**
     * Checks if client authentication is enabled.
     */
    boolean clientAuthEnabled();

}


