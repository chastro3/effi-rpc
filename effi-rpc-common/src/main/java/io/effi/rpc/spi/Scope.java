package io.effi.rpc.spi;

/**
 * Defines the scope of an SPI extension.
 */
public enum Scope {

    /**
     * Single shared instance across the application.
     */
    SINGLETON,

    /**
     * New instance for every usage.
     */
    PROTOTYPE
}


