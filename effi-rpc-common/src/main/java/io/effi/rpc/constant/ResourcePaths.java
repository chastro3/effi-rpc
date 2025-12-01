package io.effi.rpc.constant;

/**
 * Holds standard resource paths.
 */
public interface ResourcePaths {

    /**
     * Root directory for Effi-RPC internal resources.
     */
    String INTERNAL_DIR = "META-INF/effi-rpc/";

    /**
     * SPI implementation directory (enhanced SPI mechanism).
     */
    String SPI_SERVICES_DIR = INTERNAL_DIR + "services/";

    /**
     * Directory for GraalVM native-image metadata.
     */
    String NATIVE_IMAGE_DIR = "META-INF/native-image/";

    /**
     * Path to the version file.
     */
    String VERSION_FILE = INTERNAL_DIR + "version";

    /**
     * Path to the component descriptor properties file.
     */
    String COMPONENT_DESCRIPTOR_FILE = INTERNAL_DIR + "component.properties";
}
