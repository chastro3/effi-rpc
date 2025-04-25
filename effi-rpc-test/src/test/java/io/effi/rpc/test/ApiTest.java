package io.effi.rpc.test;

import io.effi.rpc.config.HierarchicalNodeConfig;
import io.effi.rpc.constant.Component;
import io.effi.rpc.contract.annotation.AnnotationCalleeBuilder;
import io.effi.rpc.contract.annotation.AnnotationStyle;
import io.effi.rpc.contract.module.EffRpcApplication;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.engine.*;
import io.effi.rpc.nativetools.ConditionItem;
import io.effi.rpc.nativetools.JsonWriter;
import io.effi.rpc.nativetools.ReflectConfigItem;
import io.effi.rpc.protocol.http.h2.Http2Callee;
import io.effi.rpc.protocol.http.h2.Http2CalleeBuilder;
import io.effi.rpc.protocol.http.h2.Http2ServerConfig;
import io.effi.rpc.test.filter.CallerReqFilter;
import io.effi.rpc.test.service.HelloClient;
import io.effi.rpc.test.service.HelloService;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiTest {

    EffRpcApplication application = new EffRpcApplication("test");

    @Test
    public void serverExport() throws IOException {
        EffRpcApplication application = new EffRpcApplication("test");
        EffiRpcModule module = application.newModule();
        module.registerShared(DefaultRegistryConfig.builder().url("consul://127.0.0.1:8500").build());
        DefaultServerExporter exporter = DefaultServerExporter.builder()
                .exportedPort(8090)
                .module(module)
                .serverConfig(Http2ServerConfig.defaultConfig())
                .build();
        ComplexRemoteService<HelloService> remoteService = new ComplexRemoteService<>(new HelloService());
        Http2Callee<HelloService> hello = new AnnotationCalleeBuilder<>(remoteService, "hello", String.class, int.class)
                .useStyle(Component.AnnotationStyle.JAX_RS)
                .build(Http2CalleeBuilder::new);
        exporter.callee(hello);
        hello.addFilter(new CallerReqFilter());
        application.start();
    }

    @Test
    public void annotatedRemoteService() {
        AnnotationRemoteService<HelloService> remoteService = new AnnotationRemoteService<>(new HelloService(), application);
        System.out.println(remoteService);
    }

    @Test
    public void annotationRemoteCaller() {
        AnnotationRemoteClient<HelloClient> remoteCaller = new AnnotationRemoteClient<>(HelloClient.class, application);
        System.out.println(remoteCaller);
    }

    @Test
    public void configTest() {
        HierarchicalNodeConfig node1 = new HierarchicalNodeConfig(null);
        HierarchicalNodeConfig node2 = new HierarchicalNodeConfig(null);
        HierarchicalNodeConfig node3 = new HierarchicalNodeConfig(null);
        node3.setParent(node2);
        node2.setParent(node1);
        node1.set("path", "/1");
        node2.set("path", "/2");
        node3.set("path", "/3");
        System.out.println(node3.getSelfPreferred("path"));
        System.out.println(node3.getParentPreferred("path"));
        System.out.println(Arrays.toString(node3.getCascaded("path").stream().toArray()));

    }

    @Test
    public void annotationStyle() throws IOException {
        ExecutorService executorService = Executors.newFixedThreadPool(200);
        for (int i = 0; i < 200; i++) {
            executorService.execute(() -> {
                AnnotationStyle springMvc = AnnotationStyle.getInstance("spring-mvc");
                AnnotationStyle springWebflux = AnnotationStyle.getInstance("spring-webflux");
                AnnotationStyle annotationStyle = AnnotationStyle.getInstance(Component.AnnotationStyle.JAX_RS);
                System.out.println(springMvc);
                System.out.println(springWebflux);
                System.out.println(annotationStyle);
            });
        }
        System.in.read();
    }

    @Test
    public void jsonTest() {
        ReflectConfigItem reflectConfigItem = new ReflectConfigItem().type("io.effi.rpc.config.FlatConfig")
                .condition(new ConditionItem().typeReachable("io.effi.rpc.config.Config"))
                .method("<init>", null)
                .method("hello", List.of("java.lang.String"));
        try  {
            JsonWriter w = new JsonWriter(new BufferedWriter(new FileWriter("out.json")));
            Map<String, Object> data = reflectConfigItem.toMap();
            w.write(data).flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
