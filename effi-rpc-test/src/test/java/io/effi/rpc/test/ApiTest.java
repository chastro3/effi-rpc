package io.effi.rpc.test;

import io.effi.rpc.base.ThreadPool;
import io.effi.rpc.base.annotation.AnnotationStyle;
import io.effi.rpc.base.parameter.MethodMapper;
import io.effi.rpc.base.parameter.ParamVar;
import io.effi.rpc.boot.AnnotationRemoteClient;
import io.effi.rpc.boot.AnnotationRemoteService;
import io.effi.rpc.boot.ComplexRemoteService;
import io.effi.rpc.boot.DefaultServiceHost;
import io.effi.rpc.component.EffiRpcApplication;
import io.effi.rpc.component.EffiRpcPlatform;
import io.effi.rpc.config.HierarchicalNodeConfig;
import io.effi.rpc.config.registry.DefaultRegistryConfig;
import io.effi.rpc.constant.Component;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.nativetools.ConditionItem;
import io.effi.rpc.nativetools.ReflectConfigItem;
import io.effi.rpc.protocol.http.arg.api.HttpMethodMapperBuilder;
import io.effi.rpc.protocol.http.h2.Http2Callee;
import io.effi.rpc.protocol.http.h2.Http2Caller;
import io.effi.rpc.protocol.http.h2.Http2ClientConfig;
import io.effi.rpc.protocol.http.h2.Http2ServerConfig;
import io.effi.rpc.protocol.http.support.HttpVersion;
import io.effi.rpc.test.service.HelloClient;
import io.effi.rpc.test.service.HelloService;
import io.effi.rpc.transport.Protocol;
import io.effi.rpc.util.JavaVersion;
import io.effi.rpc.util.TypeToken;
import io.netty.handler.codec.http.HttpMethod;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiTest {

    private static final Logger logger = LoggerFactory.getLogger(ApiTest.class);

    EffiRpcApplication application = EffiRpcPlatform.getInstance()
            .newApplication("test");

    @Test
    public void serverExport() throws Exception {
        // 创建一个service host
        DefaultServiceHost serviceHost = DefaultServiceHost.builder()
                .exportedAddress("192.168.188.1", 8090)
                .serverConfig(Http2ServerConfig.defaultConfig())
                .registryAt(DefaultRegistryConfig.builder().url("consul://127.0.0.1:8500").build())
                .build();
        ComplexRemoteService<HelloService> remoteService = new ComplexRemoteService<>(new HelloService());
        MethodMapper<HelloService> methodMapper = new HttpMethodMapperBuilder<>(remoteService, "hello")
                .mappedParameterType(String.class, ParamVar.source("name"))
                .build();
        Http2Callee callee = Http2Callee.builder(methodMapper, new HierarchicalNodeConfig())
                .path("/hello")
                .module(application.defaultModule())
                .compression("xxx")
                .method(HttpMethod.POST)
                .addResponseHeader("zzz", "hahah")
                .serialization("json")
                .desc("xxx")
                .threadPool(new ThreadPool("test", Executors.newFixedThreadPool(10)))
                .build();
        serviceHost.start().join();
        new CountDownLatch(1).await();
        logger.info("service host {}", serviceHost);
    }

    @Test
    public void caller(){
        Http2Caller<String> caller = Http2Caller.<String>builder(new TypeToken<>() {}, new HierarchicalNodeConfig())
                .path("//hello")
                .module(application.defaultModule())
                .compression("xxx")
                .clientConfig(Http2ClientConfig.defaultConfig())
                .addRequestHeader("zzz","ahahah")
                .remoteApplication("provifer")
                .build();
        System.out.println(caller);
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
            // JsonWriter w = new JsonWriter(new BufferedWriter(new FileWriter("out.json")));
            Map<String, Object> data = reflectConfigItem.toMap();
            //w.write(data).flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void spiTest() {
        Protocol protocol = EffiRpcPlatform.getInstance().getExtension(Protocol.class, HttpVersion.HTTP_2_0.protocolName());
        System.out.println(protocol);
    }

    @Test
    public void httpMethod() {
        long start = System.currentTimeMillis();
        System.out.println(HttpMethod.POST.name());
        //System.out.println("POST");
        System.out.println(System.currentTimeMillis() - start + "ms");
    }

    @Test
    public void javaVersion() {
        System.out.println(JavaVersion.get());

    }

}
