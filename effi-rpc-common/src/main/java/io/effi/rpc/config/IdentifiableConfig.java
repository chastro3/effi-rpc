package io.effi.rpc.config;

import io.effi.rpc.option.Options;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.trait.Identifiable;
import io.effi.rpc.util.StringUtil;

import java.util.UUID;

/**
 * Provides an abstract base class for identifiable configurations.
 * <p>
 * Combines configuration management with unique identification capabilities,
 * enabling configs to be uniquely identified and accessed through the Config.Supplier interface.
 */
public abstract class IdentifiableConfig implements Identifiable, Options.Supplier {

    private final String id;

    private final Options options;

    protected IdentifiableConfig(String id, Options options) {
        this.id = AssertUtil.notBlank(id, "id");
        this.options = options;
    }

    /**
     * Returns the supplied identifier or generates one with the specified prefix.
     *
     * @param id candidate identifier
     * @param prefix fallback identifier prefix
     * @return supplied identifier or generated identifier
     */
    public static String checkId(String id, String prefix) {
        if (StringUtil.isNotBlank(id)) return id;
        return prefix + "-" + UUID.randomUUID();
    }

    @Override
    public Options options() {
        return options;
    }

    @Override
    public String id() {
        return id;
    }

    /**
     * Builds {@link IdentifiableConfig} instance and defines configuration.
     */
    public abstract static class Builder<T, SELF extends Builder<T, SELF>> implements Options.Builder<T, SELF>, Identifiable {

        protected String id;

        protected Options options = Options.create();

        /**
         * Sets the configuration identifier.
         *
         * @param id configuration identifier
         * @return this builder
         */
        public SELF id(String id) {
            this.id = id;
            return self();
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public Options options() {
            return options;
        }
    }
}
