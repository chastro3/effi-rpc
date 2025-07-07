package demo.consumer;

import demo.consumer.model.ParentObject;
import io.effi.rpc.boot.AnnotationRemoteClient;
import io.effi.rpc.component.EffiRpcApplication;
import io.effi.rpc.component.EffiRpcPlatform;
import io.effi.rpc.config.registry.DefaultRegistryConfig;
import io.effi.rpc.config.registry.RegistryConfig;
import io.effi.rpc.config.transport.CertificateConfig;
import io.effi.rpc.config.transport.ClientConfig;
import io.effi.rpc.config.transport.DefaultCertificateConfig;
import io.effi.rpc.constant.Tags;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.protocol.http.h1.Http1ClientConfig;
import io.effi.rpc.protocol.http.h2.Http2ClientConfig;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.effi.rpc.constant.Component.Protocol.HTTP_1_1;

public class Consumer {

    private static final Logger logger = LoggerFactory.getLogger(Consumer.class);

    public static void main(String[] args) {

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("Enter command (start, stop, exit): ");
                String command = scanner.nextLine().trim().toLowerCase();
                if (command.equals("start")) {
                    send();
                }
                if (command.equals("stop")) {
                    EffiRpcPlatform.getInstance().stop();
                }
            }
        }

//        helloClient.helloListAsync("哈哈哈222哈", "xxxx", ParentObject.getObjList("client list"))
//                .thenAccept(System.out::println);
//        EffiRpcModule module = application.newModule();
//        module.registerShared(DefaultRegistryConfig.builder().url("consul://127.0.0.1:8500").build());
//        HierarchicalNodeConfig nodeConfig = new HierarchicalNodeConfig();
//        Http2Caller<List<ParentObject>> caller = Http2Caller.<List<ParentObject>>builder(new TypeToken<>() {}, nodeConfig)
//                .path("helloList")
//                .serialization("json")
//                .clientConfig(Http2ClientConfig.defaultConfig())
//                .module(application.defaultModule())
//                .locator(RegistryLocator.getInstance("provider"))
//                .build();
//        application.start();
//        ParamVar<Argument.Target> paramVar = ParamVar.target(Map.of("name", "123456", "age", "24"));
//        ExecutorService executorService = Executors.newFixedThreadPool(200);
//        for (int i = 0; i < 1; i++) {
//            executorService.execute(() -> {
//                long start = System.currentTimeMillis();
//                List<ParentObject> consumerList = caller.blockingCall(paramVar, Body.wrap(ParentObject.getObjList("consumer list")));
//                long end = System.currentTimeMillis();
//                System.out.println("effi-rpc 耗时:" + (end - start) + consumerList);
//                CallerMetrics callerMetrics = caller.get(CallerMetrics.GENERIC_KEY);
//                System.out.println("平均耗时:" + callerMetrics.averageCallTime());
//                start = System.currentTimeMillis();
//                //List<ParentObject> clientList = consumer.http2Test(ParentObject.getObjList("client list"));
//                end = System.currentTimeMillis();
//                //System.out.println("h2 耗时:" + (end - start) + clientList);
//            });
//        }

//        application.stop();
//        printUserThreads();
    }

    private static void send() {
        EffiRpcApplication application = EffiRpcPlatform.getInstance()
                .newApplication("consumer");
        CertificateConfig certificateConfig = DefaultCertificateConfig.builder()
                .name("client-cert")
                .certChainPath("C:\\Users\\zhouwenbo\\Desktop\\rpc\\certs\\client-cert.pem")
                .privateKeyPath("C:\\Users\\zhouwenbo\\Desktop\\rpc\\certs\\client-private-key.pem")
                .trustCertPath("C:\\Users\\zhouwenbo\\Desktop\\rpc\\certs\\ca-cert.pem")
                .build();
        Http2ClientConfig http2ClientConfig = Http2ClientConfig.builder()
                .name("h2-client")
                .ssl(false)
                .certificate(certificateConfig)
                .build();
        application.platform()
                .register(ClientConfig.class, http2ClientConfig)
                .register(ClientConfig.class, Http1ClientConfig.builder().name("hello-client").ssl(false).certificate(certificateConfig).protocol(HTTP_1_1).build());
        application.register(RegistryConfig.class, DefaultRegistryConfig.builder().url("consul://127.0.0.1:8500").build().addTags(Tags.CONSUMER, Tags.FORCE_ACTIVE))
                .register(RegistryConfig.class, DefaultRegistryConfig.builder().url("nacos://127.0.0.1:8848").build());
        AnnotationRemoteClient<HelloClient> remoteCaller = new AnnotationRemoteClient<>(HelloClient.class, application);
        HelloClient helloClient = remoteCaller.get();
//        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
//        scheduledExecutorService.scheduleAtFixedRate(() -> {
//
//        },0,1, TimeUnit.SECONDS);
        ExecutorService executorService = Executors.newFixedThreadPool(200);
        for (int i = 0; i < 200; i++) {
            executorService.execute(() -> {
                // System.out.println(helloClient.hello("native rpc", 21));
                List<ParentObject> parentObjects = helloClient.helloList("哈哈哈哈", "xxxx", ParentObject.getObjList("client list"));
                logger.info("http {}", parentObjects);
                List<ParentObject> parentObjects1 = helloClient.helloListAsync("哈哈哈哈", "xxxx", ParentObject.getObjList("async client list")).join();
                logger.info("h2 {}", parentObjects1);
            });
        }
    }
}
