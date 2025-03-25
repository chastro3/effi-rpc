package demo.consumer;

import demo.consumer.model.ParentObject;
import io.effi.rpc.contract.module.EffRpcApplication;
import io.effi.rpc.support.AnnotationRemoteCaller;
import io.effi.rpc.support.DefaultRegistryConfig;

import java.util.List;
import java.util.Map;

public class Consumer {

    public static void main(String[] args) {
        EffRpcApplication application = new EffRpcApplication("consumer");
        application.defaultModule()
                .registerShared(DefaultRegistryConfig.builder().url("consul://127.0.0.1:8500").build());
        AnnotationRemoteCaller<HelloClient> remoteCaller = new AnnotationRemoteCaller<>(HelloClient.class, application);
        HelloClient helloClient = remoteCaller.get();
        List<ParentObject> parentObjects = helloClient.helloList("哈哈哈哈", "xxxx", ParentObject.getObjList("client list"));
        System.out.println(parentObjects);
        helloClient.helloListAsync("哈哈哈222哈", "xxxx", ParentObject.getObjList("client list"))
                .thenAccept(System.out::println);
//        EffiRpcModule module = application.newModule();
//        module.registerShared(DefaultRegistryConfig.builder().url("consul://127.0.0.1:8500").build());
//        Http2Caller<List<ParentObject>> caller = Http2Caller.<List<ParentObject>>builder(new TypeToken<>() {}, new Config())
//                .path("helloList")
//                .serialization(JSON)
//                .clientConfig(Http2ClientConfig.defaultConfig())
//                .module(module)
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

        //application.stop();
        // printUserThreads();
    }

    public static void printUserThreads() {
        // 获取所有线程的堆栈信息
        Map<Thread, StackTraceElement[]> allThreads = Thread.getAllStackTraces();

        // 过滤并打印用户线程
        for (Thread thread : allThreads.keySet()) {
            // 排除 JVM 内部线程
            System.out.println("Thread Name: " + thread.getName());
            System.out.println("Thread State: " + thread.getState());
            System.out.println("Is Daemon: " + thread.isDaemon());
            System.out.println("Priority: " + thread.getPriority());
            System.out.println("-----------------------------");
        }
    }
}
