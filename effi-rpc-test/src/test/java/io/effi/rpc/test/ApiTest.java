package io.effi.rpc.test;

import io.effi.rpc.context.annotation.AnnotationStyle;
import io.effi.rpc.context.parameter.MethodMapper;
import io.effi.rpc.context.parameter.ParamVar;
import io.effi.rpc.boot.AnnotationRemoteClient;
import io.effi.rpc.boot.AnnotationRemoteService;
import io.effi.rpc.boot.ApplicationServiceRegistrar;
import io.effi.rpc.boot.ComplexRemoteService;
import io.effi.rpc.boot.ServerLauncher;
import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.registry.DefaultRegistryConfig;
import io.effi.rpc.component.registry.RegistryConfig;
import io.effi.rpc.component.support.ThreadPool;
import io.effi.rpc.config.ConfigValues;
import io.effi.rpc.config.DefaultHierarchicalConfig;
import io.effi.rpc.constant.EffiRpcFramework;
import io.effi.rpc.constant.Tags;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.nativetools.ConditionItem;
import io.effi.rpc.nativetools.ReflectConfig;
import io.effi.rpc.protocol.http.arg.api.HttpMethodMapperBuilder;
import io.effi.rpc.protocol.http.h2.Http2Callee;
import io.effi.rpc.protocol.http.h2.Http2Caller;
import io.effi.rpc.protocol.http.h2.Http2ClientConfig;
import io.effi.rpc.protocol.http.h2.Http2Protocol;
import io.effi.rpc.protocol.http.h2.Http2ServerConfig;
import io.effi.rpc.test.service.HelloClient;
import io.effi.rpc.test.service.HelloService;
import io.effi.rpc.transport.TransportProtocol;
import io.effi.rpc.util.TypeCapture;
import io.netty.handler.codec.http.HttpMethod;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiTest {

    private static final Logger logger = LoggerFactory.getLogger(ApiTest.class);

    ScopedApplication application = ScopedPlatform.defaultPlatform()
            .newApplication("test");

    @Test
    public void serverExport() throws Exception {
        ScopedModule module = application.defaultModule();
        // 创建一个service host
        ServerLauncher serverLauncher = ServerLauncher.allocate(application, Http2ServerConfig.defaultConfig(), "192.168.188.1", 8090);
        ApplicationServiceRegistrar serviceRegistrar = new ApplicationServiceRegistrar(application);
        application.platform().registry().register(RegistryConfig.class, DefaultRegistryConfig.builder().authority("consul://127.0.0.1:8500").build().addTags(Tags.PROVIDER, Tags.FORCE_ACTIVE));
        ComplexRemoteService<HelloService> remoteService = new ComplexRemoteService<>(new HelloService());
        MethodMapper<HelloService> methodMapper = new HttpMethodMapperBuilder<>(remoteService, "hello")
                .mappedParameterType(String.class, ParamVar.source("name"))
                .build();
        Http2Callee callee = Http2Callee.builder(methodMapper, new DefaultHierarchicalConfig())
                .path("/hello")
                .module(module)
                .compression("xxx")
                .method(HttpMethod.POST)
                .addResponseHeader("zzz", "hahah")
                .serialization("json")
                .desc("xxx")
                .threadPool(new ThreadPool("test", Executors.newFixedThreadPool(10)))
                .build();
        serviceRegistrar.register();
        new CountDownLatch(1).await();
        logger.info("service host {}", serverLauncher);
    }

    @Test
    public void caller(){
        Http2Caller<String> caller = Http2Caller.<String>builder(new TypeCapture<>() {}, new DefaultHierarchicalConfig())
                .path("//hello")
                .module(application.defaultModule())
                .compression("xxx")
                .clientConfig(Http2ClientConfig.defaultConfig())
                .addRequestHeader("zzz","ahahah")
                .target("provifer")
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
        DefaultHierarchicalConfig node1 = new DefaultHierarchicalConfig(null);
        DefaultHierarchicalConfig node2 = new DefaultHierarchicalConfig(null);
        DefaultHierarchicalConfig node3 = new DefaultHierarchicalConfig(null);
        node3.withParent(node2);
        node2.withParent(node1);
        node1.set("path", "/1");
        node2.set("path", "/2");
        node3.set("path", "/3");

    }

    @Test
    public void annotationStyle() throws IOException {
        ExecutorService executorService = Executors.newFixedThreadPool(200);
        for (int i = 0; i < 200; i++) {
            executorService.execute(() -> {
                AnnotationStyle springMvc = AnnotationStyle.getInstance("spring-mvc");
                AnnotationStyle springWebflux = AnnotationStyle.getInstance("spring-webflux");
                AnnotationStyle annotationStyle = AnnotationStyle.getInstance(ConfigValues.AnnotationStyle.JAX_RS);
                System.out.println(springMvc);
                System.out.println(springWebflux);
                System.out.println(annotationStyle);
            });
        }
        System.in.read();
    }

    @Test
    public void jsonTest() {
        ReflectConfig.Item reflectConfigItem = new ReflectConfig.Item().type("io.effi.rpc.config.FlatConfig")
                .condition(new ConditionItem().typeReached("io.effi.rpc.config.Config"))
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
        TransportProtocol protocol = ScopedPlatform.defaultPlatform().namedExtension(TransportProtocol.class, Http2Protocol.VERSION.name());
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
        System.out.println(EffiRpcFramework.javaVersion());

    }

}
