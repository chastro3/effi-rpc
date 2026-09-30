package demo.provider.interfaceapi;

import demo.api.InterfaceHelloService;
import io.effi.rpc.annotation.rpc.ServeGroup;
import io.effi.rpc.protocol.http.h1.Http1Protocol;

/**
 * Implementation used by the provider-side RPC example.
 */
@ServeGroup(interfaces = InterfaceHelloService.class, protocol = {Http1Protocol.NAME})
public final class InterfaceHelloServiceImpl implements InterfaceHelloService {

    @Override
    public String hello(String name, int age) {
        return "hello " + name + ", age: " + age;
    }
}
