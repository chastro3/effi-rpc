package io.effi.rpc.spi;

/**
 * Defines the scope of an SPI extension.
 * <p>
 * The scope can either be:
 * <ul>
 *   <li>{@link SINGLETON}: A single instance of the extension is shared and reused.</li>
 *   <li>{@link PROTOTYPE}: A new instance of the extension is created each time it is needed.</li>
 * </ul>
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

