package io.effi.rpc.test;

import io.effi.rpc.common.config.NodeConfig;
import io.effi.rpc.common.constant.Component;
import io.effi.rpc.contract.annotation.AnnotationCalleeBuilder;
import io.effi.rpc.contract.module.EffRpcApplication;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.engine.*;
import io.effi.rpc.protocol.http.h2.Http2Callee;
import io.effi.rpc.protocol.http.h2.Http2CalleeBuilder;
import io.effi.rpc.protocol.http.h2.Http2ServerConfig;
import io.effi.rpc.test.filter.CallerReqFilter;
import io.effi.rpc.test.service.HelloClient;
import io.effi.rpc.test.service.HelloService;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

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
        NodeConfig node1 = new NodeConfig(null);
        NodeConfig node2 = new NodeConfig(null);
        NodeConfig node3 = new NodeConfig(null);
        node3.setParent(node2);
        node2.setParent(node1);
        node1.set("path", "/1");
        node2.set("path", "/2");
        node3.set("path", "/3");
        System.out.println(node3.getSelfPreferred("path"));
        System.out.println(node3.getParentPreferred("path"));
        System.out.println(Arrays.toString(node3.getCascaded("path").stream().toArray()));

    }

}
