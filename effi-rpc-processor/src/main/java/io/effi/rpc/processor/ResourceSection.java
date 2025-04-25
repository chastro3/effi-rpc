package io.effi.rpc.processor;

import java.io.IOException;

/**
 * Defines the contract for resource sections.
 */
public interface ResourceSection {

    /**
     * Writes the content of the resource section.
     *
     * @throws IOException if an error occurs during writing
     */
    void write() throws IOException;
}

