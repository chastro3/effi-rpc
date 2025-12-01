package io.effi.rpc.component.registry;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.component.TagComponent;
import io.effi.rpc.component.support.ThreadPool;
import io.effi.rpc.config.IdentifiableConfig;
import io.effi.rpc.config.OptionName;
import io.effi.rpc.config.Options;
import io.effi.rpc.config.SmartURL;
import io.effi.rpc.trait.Identifiable;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Defines configurations for registry clients and operations.
 * <p>
 * Provides a standardized interface for registry configuration including
 * type, address, and thread pool settings for registry operations.
 */
@ScopedComponent(scope = PLATFORM)
public interface RegistryConfig extends Options.Supplier, Identifiable, TagComponent {

    OptionName<Integer> CONNECT_TIMEOUT = OptionName.of("connectTimeout", 3000);

    OptionName<Integer> RETRIES = OptionName.of("retries", 3);

    OptionName<Integer> HEARTBEAT_INTERVAL =OptionName.of("heartbeatInterval", 5000);

    /**
     * Return the registry type.
     */
    String type();

    /**
     * Returns the registry address or a list of addresses.
     * If multiple addresses are specified, they must be separated by ','.
     */
    String address();

    /**
     * Returns the thread pool for registry operations.
     */
    ThreadPool threadPool();

    /**
     * Builds {@link RegistryConfig} instance and defines configuration.
     */
    abstract class Builder<T extends RegistryConfig, SELF extends Builder<T, SELF>>
            extends IdentifiableConfig.Builder<T, SELF> {

        protected String type;

        protected String address;

        protected ThreadPool threadPool;

        /**
         * Sets the registry type (eg: 'consul','nacos').
         */
        public SELF type(String type) {
            this.type = type;
            return self();
        }

        /**
         * Sets the registry address or addresses.
         * If multiple addresses are specified, they must be separated by ','.
         */
        public SELF address(String address) {
            this.address = address;
            return self();
        }

        /**
         * Sets the registry authority from a URL string.
         * (eg: 'consul://127.0.0.1:8500')
         */
        public SELF authority(String authority) {
            SmartURL smartUrl = SmartURL.valueOf(authority);
            type(smartUrl.scheme());
            address(smartUrl.address());
            return self();
        }

        /**
         * Sets the thread pool for registry operations.
         */
        public SELF threadPool(ThreadPool threadPool) {
            this.threadPool = threadPool;
            return self();
        }

        /**
         * Sets the thread pool for registry operations.
         *
         * @param connectTimeout the connection timeout in milliseconds
         */
        public SELF connectTimeout(int connectTimeout) {
            addOption(CONNECT_TIMEOUT, connectTimeout);
            return self();
        }

        /**
         * Sets the number of retry attempts.
         */
        public SELF retries(int retries) {
            addOption(RETRIES, retries);
            return self();
        }

        /**
         * Sets the heartbeat interval configuration.
         */
        public SELF heartbeatInterval(int heartbeatInterval) {
            addOption(HEARTBEAT_INTERVAL, heartbeatInterval);
            return self();
        }
    }
}
