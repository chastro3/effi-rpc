package io.effi.rpc.component;

/**
 * Provides access to a {@link EffiRpcPlatform} instance.
 */
public interface PlatformSource {

    EffiRpcPlatform platform();

    /**
     * Holds and provides an {@link EffiRpcPlatform} instance.
     */
    abstract class Holder implements PlatformSource {
        protected EffiRpcPlatform platform;

        public Holder(EffiRpcPlatform platform) {
            this.platform = platform == null ? EffiRpcPlatform.currentPlatform() : platform;
        }

        @Override
        public EffiRpcPlatform platform() {
            return platform;
        }
    }
}
