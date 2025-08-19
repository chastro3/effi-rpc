package io.effi.rpc.component.transport;

/**
 * Defines configurations for certificates used in secure connections.
 * <p>
 * Provides a standardized interface for managing certificate chains,
 * private keys, and trust certificates for TLS/SSL connections.
 */
public interface CertificateConfig {

    /**
     * Returns the name of the certificate.
     */
    String name();

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


