package io.effi.rpc.config;

import io.effi.rpc.annotation.spi.Extensible;

/**
 * Maps custom protocol names to standard protocol names.
 */
@Extensible(lazyLoad = false)
public interface URLProtocolMapper {

    /**
     * Returns the supported protocol name.
     */
    String supported();

    /**
     * Returns the mapped protocol name.
     */
    String mapped();
}

