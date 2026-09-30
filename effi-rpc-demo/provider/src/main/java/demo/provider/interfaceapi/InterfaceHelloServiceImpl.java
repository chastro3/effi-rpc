package demo.provider.interfaceapi;

import demo.api.InterfaceHelloService;
import io.effi.rpc.protocol.http.h1.Http1Protocol;
import io.effi.rpc.spring.EffiRpcService;

/**
 * Implementation used by the provider-side RPC example.
 */
@EffiRpcService(interfaces = InterfaceHelloService.class, protocols = Http1Protocol.NAME)
public final class InterfaceHelloServiceImpl implements InterfaceHelloService {

    @Override
    public String hello(String name, int age) {
        return "hello " + name + ", age: " + age;
    }
}
