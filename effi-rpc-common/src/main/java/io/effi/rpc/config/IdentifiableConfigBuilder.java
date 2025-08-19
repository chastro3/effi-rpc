package io.effi.rpc.config;

/**
 * Builds {@link IdentifiableConfig} instance and defines configuration.
 */
public abstract class IdentifiableConfigBuilder<T, SELF extends IdentifiableConfigBuilder<T, SELF>>
        implements ConfigBuilder<T, SELF> {

    protected String id;

    protected Config config = new DefaultConfig();

    public SELF id(String id) {
        this.id = id;
        return self();
    }

    @Override
    public Config config() {
        return config;
    }
}
