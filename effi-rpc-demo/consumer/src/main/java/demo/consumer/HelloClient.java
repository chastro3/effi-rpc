package demo.consumer;

import demo.consumer.model.ParentObject;
import io.effi.rpc.constant.Component;
import io.effi.rpc.contract.annotation.Body;
import io.effi.rpc.contract.annotation.EffiRpcCaller;
import io.effi.rpc.contract.annotation.EffiRpcClient;
import jakarta.ws.rs.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@EffiRpcClient(
        application = "provider",
        protocol = "http",
        clientConfig = "hello-client",
        path = "service",
        style = Component.AnnotationStyle.JAX_RS,
        address = "127.0.0.1:8090"
)
public interface HelloClient {

    @GET
    @Path("hello")
    String hello(@QueryParam("name") String name, @QueryParam("age") Integer age);

    @POST
    @Path("helloList")
    @EffiRpcCaller(path = "helloList", protocol = "http", style = Component.AnnotationStyle.JAX_RS)
    List<ParentObject> helloList(@QueryParam("name") String name,
                                 @HeaderParam("content-type11") String contentType,
                                 @Body List<ParentObject> list);

    @POST
    @Path("helloList")
    @EffiRpcCaller(path = "helloList", protocol = "h2", style = Component.AnnotationStyle.JAX_RS)
    CompletableFuture<List<ParentObject>> helloListAsync(@QueryParam("name") String name,
                                                         @HeaderParam("content-type111") String contentType,
                                                         @Body List<ParentObject> list);
}
