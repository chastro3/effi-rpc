package io.effi.rpc.component;

/**
 * Provides access to a {@link EffiRpcApplication} instance.
 */
public interface ApplicationSource extends PlatformSource {

    EffiRpcApplication application();

    @Override
    default EffiRpcPlatform platform() {
        return application().platform();
    }

    interface Aware extends ApplicationSource{

        void setApplication(EffiRpcApplication application);
    }

    /**
     * Holds and provides an {@link EffiRpcApplication} instance.
     */
    abstract class Holder implements ApplicationSource {

        protected EffiRpcApplication application;

        public Holder(EffiRpcApplication application) {
            this.application = application;
        }

        @Override
        public EffiRpcApplication application() {
            return application;
        }
    }
}
