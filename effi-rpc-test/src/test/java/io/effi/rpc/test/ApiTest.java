package io.effi.rpc.test;

import io.effi.rpc.boot.AnnotationCallerGroup;
import io.effi.rpc.boot.AnnotationServantGroup;
import io.effi.rpc.boot.ApplicationServiceRegistrar;
import io.effi.rpc.boot.ComplexServantGroup;
import io.effi.rpc.boot.ServerLauncher;
import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.registry.DefaultRegistryConfig;
import io.effi.rpc.component.registry.RegistryConfig;
import io.effi.rpc.component.support.ThreadPool;
import io.effi.rpc.constant.EffiRpcFramework;
import io.effi.rpc.constant.Tags;
import io.effi.rpc.context.annotation.AnnotationStyle;
import io.effi.rpc.context.parameter.ServantMethod;
import io.effi.rpc.context.parameter.ParamVar;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.nativetools.ConditionItem;
import io.effi.rpc.nativetools.ReflectConfig;
import io.effi.rpc.protocol.http.arg.annotation.jax.JaxRsStyleResolver;
import io.effi.rpc.protocol.http.arg.api.HttpServantMethodBuilder;
import io.effi.rpc.protocol.http.h2.Http2Servant;
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

    ScopedApplication application = ScopedPlatform.defaultInstance()
            .newApplication("test");

    @Test
    public void serverExport() throws Exception {
        ScopedModule module = application.defaultModule();
        // 创建一个service host
        ServerLauncher serverLauncher = ServerLauncher.attach(application, Http2ServerConfig.defaultConfig(), "192.168.188.1", 8090);
        ApplicationServiceRegistrar serviceRegistrar = new ApplicationServiceRegistrar(application);
        application.platform().registry().register(RegistryConfig.class, DefaultRegistryConfig.builder().authority("consul://127.0.0.1:8500").build().addTags(Tags.PROVIDER, Tags.FORCE_ACTIVE));
        ComplexServantGroup<HelloService> remoteService = new ComplexServantGroup<>(new HelloService());
        ServantMethod<HelloService> servantMethod = new HttpServantMethodBuilder<>(remoteService, "hello")
                .mappedParameterType(String.class, ParamVar.source("id"))
                .build();
        Http2Servant callee = Http2Servant.builder(servantMethod)
                .path("/hello")
                .module(module)
                .compressor("xxx")
                .method(HttpMethod.POST)
                .addResponseHeader("zzz", "hahah")
                .serializer("json")
                .label("xxx")
                .threadPool(new ThreadPool("test", Executors.newFixedThreadPool(10)))
                .build();
        serviceRegistrar.register();
        new CountDownLatch(1).await();
        logger.info("service host {}", serverLauncher);
    }

    @Test
    public void caller(){
        Http2Caller<String> caller = Http2Caller.<String>builder(new TypeCapture<>() {})
                .path("//hello")
                .module(application.defaultModule())
                .compressor("xxx")
                .clientConfig(Http2ClientConfig.defaultConfig())
                .addRequestHeader("zzz","ahahah")
                .endpoint("provifer")
                .build();
        System.out.println(caller);
    }

    @Test
    public void annotatedRemoteService() {
        AnnotationServantGroup<HelloService> remoteService = new AnnotationServantGroup<>(new HelloService(), application);
        System.out.println(remoteService);
    }

    @Test
    public void annotationRemoteCaller() {
        AnnotationCallerGroup<HelloClient> remoteCaller = new AnnotationCallerGroup<>(HelloClient.class, application);
        System.out.println(remoteCaller);
    }

    @Test
    public void annotationStyle() throws IOException {
        ExecutorService executorService = Executors.newFixedThreadPool(200);
        for (int i = 0; i < 200; i++) {
            executorService.execute(() -> {
                AnnotationStyle springMvc = AnnotationStyle.getInstance("spring-mvc");
                AnnotationStyle springWebflux = AnnotationStyle.getInstance("spring-webflux");
                AnnotationStyle annotationStyle = AnnotationStyle.getInstance(JaxRsStyleResolver.NAME);
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
        TransportProtocol protocol = ScopedPlatform.defaultInstance().namedExtension(TransportProtocol.class, Http2Protocol.VERSION.name());
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
