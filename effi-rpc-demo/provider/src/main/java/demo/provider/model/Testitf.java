package demo.provider.model;

import io.effi.rpc.contract.annotation.EffiRpcService;

/**
 * @Author WenBo Zhou
 * @Date 2025/4/14 10:41
 */
@EffiRpcService
public interface Testitf{

    String sayHello(String name);

    default String defaultHello(String name) {
        return "default: " + name;
    }

    static String staticHello(String name) {
        return "static: " + name;
    }
}
