package demo.api;

/**
 * Shared plain Java interface for the interface-style RPC demo.
 */
public interface InterfaceHelloService {

    String hello(String name, int age);
}
