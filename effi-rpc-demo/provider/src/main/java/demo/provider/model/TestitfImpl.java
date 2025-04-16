package demo.provider.model;

import io.effi.rpc.contract.annotation.EffiRpcService;

@EffiRpcService
public class TestitfImpl implements Testitf {
    @Override
    public String sayHello(String name) {
        return "impl: " + name;
    }
}
