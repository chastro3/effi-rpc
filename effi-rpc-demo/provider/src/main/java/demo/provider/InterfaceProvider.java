package demo.provider;

import demo.api.InterfaceHelloService;
import demo.provider.interfaceapi.InterfaceHelloServiceImpl;
import io.effi.rpc.boot.EffiRpcBootstrap;
import io.effi.rpc.boot.InterfaceServantGroup;
import io.effi.rpc.protocol.http.h1.Http1Protocol;
import io.effi.rpc.protocol.http.h1.Http1ServerConfig;

import java.util.concurrent.CountDownLatch;

/**
 * Starts the provider for the plain interface RPC example.
 */
public final class InterfaceProvider {

    private InterfaceProvider() {
    }

    public static void main(String[] args) {
        int port = args.length == 0 ? 18092 : Integer.parseInt(args[0]);
        EffiRpcBootstrap bootstrap = EffiRpcBootstrap.newInstance("interface-provider")
                .server(Http1ServerConfig.defaultConfig(), "127.0.0.1", port);
        InterfaceServantGroup.<InterfaceHelloService>builder()
                .targetType(InterfaceHelloService.class)
                .service(new InterfaceHelloServiceImpl())
                .module(bootstrap.defaultModule())
                .protocol(Http1Protocol.NAME)
                .build();
        bootstrap.start().toCompletableFuture().join();
        System.out.println("Interface provider started on 127.0.0.1:" + port);
        try {
            new CountDownLatch(1).await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            bootstrap.stop();
        }
    }
}
