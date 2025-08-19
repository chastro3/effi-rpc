package io.effi.rpc.context;

public interface Configurable {

    interface Configurator<C extends Configurable> {

        void configure(C configurable);
    }
}
