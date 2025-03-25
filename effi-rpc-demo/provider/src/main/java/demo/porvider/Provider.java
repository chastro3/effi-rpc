package demo.porvider;

import io.effi.rpc.protocol.http.h2.Http2ServerConfig;
import io.effi.rpc.support.DefaultRegistryConfig;
import io.effi.rpc.support.EffiRpcBootstrap;

public class Provider {

    public static void main(String[] args) {
        EffiRpcBootstrap bootstrap = EffiRpcBootstrap.newInstance("provider")
                .exported(Http2ServerConfig.defaultConfig(), 8090)
                .exported(Http2ServerConfig.defaultConfig(), 8091)
                .registry(DefaultRegistryConfig.builder().url("consul://127.0.0.1:8500").build())
                .service(new HelloService())
                .start();
        System.out.println(bootstrap);

//        EffRpcApplication application = EffRpcApplication.builder().name("provider").build();
//        EffiRpcModule module = application.newModule();
//        module.registerShared(DefaultRegistryConfig.builder().url("nacos://127.0.0.1:8848").build());
//        DefaultServerExporter exporter1 = DefaultServerExporter.builder()
//                .exportedPort(8090)
//                .module(module)
//                .serverConfig(Http2ServerConfig.defaultConfig())
//                .build();
//        ComplexRemoteService<HelloService> remoteService = ComplexRemoteService.defaultBuild(null, new HelloService(), null);
//        Http2Callee<HelloService> hello = new AnnotatedCalleeBuilder<>(remoteService, "hello", String.class, int.class)
//                .useStyle(Component.RestParser.JAX_RS)
//                .build(Http2CalleeBuilder::new);
//        hello.config().set(KeyConstant.SERIALIZATION, JSON);
//        Http2Callee<HelloService> helloList = new AnnotatedCalleeBuilder<>(remoteService, "helloList", String.class, String.class, List.class)
//                .useStyle(Component.RestParser.JAX_RS)
//                .build(Http2CalleeBuilder::new);
//        helloList.config().set(KeyConstant.SERIALIZATION, JSON);
//        exporter1.callee(hello).callee(helloList);
//        application.start();
//
//        EffRpcApplication application1 = EffRpcApplication.builder().name("provider1").build();
//        EffiRpcModule effiRpcModule1 = application1.newModule();
//        effiRpcModule1.registerShared(DefaultRegistryConfig.builder().url("nacos://127.0.0.1:8848").build());
//        DefaultServerExporter exporter11 = DefaultServerExporter.builder()
//                .exportedPort(8091)
//                .module(effiRpcModule1)
//                .serverConfig(Http2ServerConfig.defaultConfig())
//                .build();
//        //exporter11.callee(hello).callee(helloList);
//        application1.start();
    }
}
