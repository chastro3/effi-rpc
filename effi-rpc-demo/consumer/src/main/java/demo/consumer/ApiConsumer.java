package demo.consumer;

import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.registry.DefaultRegistryConfig;
import io.effi.rpc.context.parameter.Header;
import io.effi.rpc.governance.registry.RegistryLocator;
import io.effi.rpc.protocol.http.h2.Http2Caller;
import io.effi.rpc.concurrent.Future;
import io.effi.rpc.util.TypeCapture;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ApiConsumer {

    public static void main(String[] args) {
        DefaultRegistryConfig consul = DefaultRegistryConfig.builder()
                .id("consul")
                .authority("consul://127.0.0.1:8500")
                .build();
        Http2Caller<String> caller = Http2Caller.<String>builder(new TypeCapture<>() {})
                .path("hello")
                .locator(RegistryLocator.cached("default", consul))
                .module(ScopedModule.defaultInstance())
                .build();
        ExecutorService executorService = Executors.newFixedThreadPool(200);
        for (int i = 0; i < 1; i++) {
            executorService.execute(() -> {
                Future<String> future = caller.call("xxx", Header.target(Map.of("content-type", "application/json")));
                System.out.println(future.toCompletableFuture().join());
            });
        }
    }
}
