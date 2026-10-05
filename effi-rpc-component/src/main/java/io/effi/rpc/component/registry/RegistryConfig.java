package io.effi.rpc.component.registry;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.component.TagComponent;
import io.effi.rpc.component.registry.options.RegistryOptions;
import io.effi.rpc.component.tools.ThreadPool;
import io.effi.rpc.config.IdentifiableConfig;
import io.effi.rpc.option.Options;
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

    /**
     * Returns the registry type.
     */
    String type();

    /**
     * Returns the registry address or a list of addresses.
     * If multiple addresses are specified, they must be separated by ','.
     * Multi-address support depends on the registry implementation.
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
         * Sets the registry type.
         *
         * @param type registry type, such as {@code consul} or {@code nacos}
         * @return this builder
         */
        public SELF type(String type) {
            this.type = type;
            return self();
        }

        /**
         * Sets the registry address or addresses.
         * <p>
         * Multiple addresses must be separated by commas.
         *
         * @param address registry address or comma-separated addresses
         * @return this builder
         */
        public SELF address(String address) {
            this.address = address;
            return self();
        }

        /**
         * Sets the registry type and address from a registry URL.
         *
         * @param authority registry URL, such as {@code consul://127.0.0.1:8500}
         * @return this builder
         */
        public SELF authority(String authority) {
            SmartURL smartUrl = SmartURL.valueOf(authority);
            type(smartUrl.scheme());
            address(smartUrl.address());
            return self();
        }

        /**
         * Sets the thread pool for registry operations.
         *
         * @param threadPool registry thread pool
         * @return this builder
         */
        public SELF threadPool(ThreadPool threadPool) {
            this.threadPool = threadPool;
            return self();
        }

        /**
         * Sets the registry connection timeout.
         *
         * @param connectTimeout connection timeout in milliseconds
         * @return this builder
         */
        public SELF connectTimeout(int connectTimeout) {
            addOption(RegistryOptions.CONNECT_TIMEOUT, connectTimeout);
            return self();
        }

        /**
         * Sets the number of retry attempts.
         *
         * @param retries retry attempts
         * @return this builder
         */
        public SELF retries(int retries) {
            addOption(RegistryOptions.RETRIES, retries);
            return self();
        }

        /**
         * Sets the registration retry interval.
         *
         * @param retryInterval retry interval in milliseconds
         * @return this builder
         */
        public SELF retryInterval(int retryInterval) {
            addOption(RegistryOptions.RETRY_INTERVAL, retryInterval);
            return self();
        }

        /**
         * Sets the heartbeat interval.
         *
         * @param heartbeatInterval heartbeat interval in milliseconds
         * @return this builder
         */
        public SELF heartbeatInterval(int heartbeatInterval) {
            addOption(RegistryOptions.HEARTBEAT_INTERVAL, heartbeatInterval);
            return self();
        }

        /**
         * Sets the timeout used while releasing registry resources.
         *
         * @param closeTimeout close timeout in milliseconds
         * @return this builder
         */
        public SELF closeTimeout(int closeTimeout) {
            addOption(RegistryOptions.CLOSE_TIMEOUT, closeTimeout);
            return self();
        }
    }
}
