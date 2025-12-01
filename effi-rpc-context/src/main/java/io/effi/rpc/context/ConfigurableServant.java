package io.effi.rpc.context;

public interface ConfigurableServant extends ConfigurablePeer, Servant {

    ConfigurableServant label(String label);

}
