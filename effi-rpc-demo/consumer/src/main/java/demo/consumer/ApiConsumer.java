package demo.consumer;

import io.effi.rpc.component.EffiRpcModule;
import io.effi.rpc.component.EffiRpcPlatform;
import io.effi.rpc.config.HierarchicalNodeConfig;
import io.effi.rpc.config.registry.DefaultRegistryConfig;
import io.effi.rpc.protocol.http.h2.Http2Caller;
import io.effi.rpc.protocol.http.h2.Http2ClientConfig;
import io.effi.rpc.util.TypeToken;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ApiConsumer {

    public static void main(String[] args) {
        EffiRpcModule module = EffiRpcPlatform.getInstance()
                .newApplication("consumer")
                .defaultModule();
        Http2Caller<String> caller = Http2Caller.<String>builder(new TypeToken<>() {}, new HierarchicalNodeConfig())
                .path("hello")
                .remoteApplication("provider")
                .registryConfigs(DefaultRegistryConfig.builder().url("consul://127.0.0.1:8500").build())
                .clientConfig(Http2ClientConfig.defaultConfig())
                .module(module)
                .build();
        ExecutorService executorService = Executors.newFixedThreadPool(200);
        for (int i = 0; i < 200; i++) {
            executorService.execute(() -> {
                CompletableFuture<String> future = caller.call("xxx");
                System.out.println(future.join());
                System.out.println(caller);
            });
        }
    }
}
