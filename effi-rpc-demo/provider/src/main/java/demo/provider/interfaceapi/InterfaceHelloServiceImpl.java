package demo.provider.interfaceapi;

import demo.api.InterfaceHelloService;

/**
 * Implementation used by the provider-side RPC example.
 */
public final class InterfaceHelloServiceImpl implements InterfaceHelloService {

    @Override
    public String hello(String name, int age) {
        return "hello " + name + ", age: " + age;
    }
}
