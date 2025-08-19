package demo.consumer;

import io.effi.rpc.async.Future;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.registry.DefaultRegistryConfig;
import io.effi.rpc.component.registry.RegistryConfig;
import io.effi.rpc.config.DefaultHierarchicalConfig;
import io.effi.rpc.protocol.http.h2.Http2Caller;
import io.effi.rpc.protocol.http.h2.Http2ClientConfig;
import io.effi.rpc.util.TypeCapture;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ApiConsumer {

    public static void main(String[] args) {
        DefaultRegistryConfig consul = DefaultRegistryConfig.builder().id("consul").authority("consul://127.0.0.1:8500").build();
        ScopedModule module = ScopedPlatform.defaultPlatform()
                .defaultApplication()
                .withName("consumer")
                .defaultModule();
        module.platform().registry().register(RegistryConfig.class, consul);
        Http2Caller<String> caller = Http2Caller.<String>builder(new TypeCapture<>() {}, new DefaultHierarchicalConfig())
                .path("hello")
                .target("provider")
                .registryConfigs("consul")
                .clientConfig(Http2ClientConfig.defaultConfig())
                .module(module)
                .build();
        ExecutorService executorService = Executors.newFixedThreadPool(200);
        for (int i = 0; i < 2000; i++) {
            executorService.execute(() -> {
                Future<String> future = caller.call("xxx");
                System.out.println(future.toCompletableFuture().join());
            });
        }
    }
}
