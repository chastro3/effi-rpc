package demo.consumer;

import demo.api.InterfaceHelloService;
import io.effi.rpc.boot.InterfaceCallerGroup;
import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.context.support.DirectLocator;
import io.effi.rpc.protocol.http.h1.Http1Protocol;

/**
 * Calls the provider for the plain interface RPC example.
 */
public final class InterfaceConsumer {

    private InterfaceConsumer() {
    }

    public static void main(String[] args) {
        int port = args.length == 0 ? 18092 : Integer.parseInt(args[0]);
        ScopedPlatform platform = new ScopedPlatform("interface-consumer-platform");
        ScopedApplication application = platform.newApplication("interface-consumer");
        try {
            InterfaceHelloService client = InterfaceCallerGroup.<InterfaceHelloService>builder()
                    .targetType(InterfaceHelloService.class)
                    .module(application.defaultModule())
                    .protocol(Http1Protocol.NAME)
                    .locator(DirectLocator.Resolver.NAME)
                    .endpoint("127.0.0.1:" + port)
                    .build()
                    .proxy();
            System.out.println(client.hello("interface", 18));
        } finally {
            platform.close();
        }
    }
}
