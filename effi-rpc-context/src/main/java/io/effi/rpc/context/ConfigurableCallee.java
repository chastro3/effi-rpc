package io.effi.rpc.context;

public interface ConfigurableCallee extends ConfigurablePeer, Callee {

    ConfigurableCallee withDesc(String desc);

}
