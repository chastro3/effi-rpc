package demo.provider;

import io.effi.rpc.engine.DefaultServerConfig;
import io.effi.rpc.engine.EffiRpcBootstrap;
import io.effi.rpc.protocol.http.h2.Http2ServerConfig;

import static io.effi.rpc.constant.Component.H2ClearTextMode.PREFACE_MODE;

public class Provider {

    public static void main(String[] args) {
        EffiRpcBootstrap bootstrap = EffiRpcBootstrap.newInstance("provider")
                .exported(Http2ServerConfig.builder().ssl(false).clearTextMode(PREFACE_MODE).build(), 8090)
                .exported(DefaultServerConfig.builder().protocol("http").build(), 8091)
                //.registry(DefaultRegistryConfig.builder().url("consul://127.0.0.1:8500").build())
                .filter(new CalleeLogFilter())
                .service(new HelloService())
                .start();
        System.out.println(bootstrap);
//        EffRpcApplication application = new EffRpcApplication("provider");
//        EffiRpcModule module = application.newModule();
//        module.registerShared(DefaultRegistryConfig.builder().url("nacos://127.0.0.1:8848").build());
//        DefaultServerExporter exporter1 = DefaultServerExporter.builder()
//                .exportedPort(8090)
//                .module(module)
//                .serverConfig(Http2ServerConfig.defaultConfig())
//                .build();
//        ComplexRemoteService<HelloService> remoteService = new ComplexRemoteService<>(new HelloService());
//        Http2Callee<HelloService> hello = new AnnotationCalleeBuilder<>(remoteService, "hello", String.class, Integer.class)
//                .useStyle(Component.AnnotationStyle.JAX_RS)
//                .build(Http2CalleeBuilder::new);
//        hello.config().set(DefaultConfigKeys.SERIALIZATION.key(), JSON);
//        Http2Callee<HelloService> helloList = new AnnotationCalleeBuilder<>(remoteService, "helloList", String.class, String.class, List.class)
//                .useStyle(Component.AnnotationStyle.JAX_RS)
//                .build(Http2CalleeBuilder::new);
//        helloList.config().set(DefaultConfigKeys.SERIALIZATION.key(), JSON);
//        helloList.export(module);
//        exporter1.callee(hello).callee(helloList);
//        application.start();
//
//        EffRpcApplication application1 = new EffRpcApplication("provider1");
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
