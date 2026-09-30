package demo.consumer;

import demo.api.InterfaceHelloService;
import io.effi.rpc.boot.EffiRpcBootstrap;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.context.options.CallerOptions;
import io.effi.rpc.context.options.GovernanceOptions;
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
        try {
            EffiRpcBootstrap bootstrap = EffiRpcBootstrap.newInstance(platform, "interface-consumer");
            InterfaceHelloService client = bootstrap.consume(
                    InterfaceHelloService.class,
                    options -> {
                        options.addOption(CallerOptions.PROTOCOL, Http1Protocol.NAME)
                                .addOption(GovernanceOptions.LOCATOR, DirectLocator.Resolver.NAME)
                                .addOption(CallerOptions.ENDPOINT, "127.0.0.1:" + port);
                    }
            );
            System.out.println(client.hello("interface", 18));
        } finally {
            platform.close();
        }
    }
}
