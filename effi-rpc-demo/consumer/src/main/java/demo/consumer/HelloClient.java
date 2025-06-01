package demo.consumer;

import demo.consumer.model.ParentObject;
import io.effi.rpc.annotation.rpc.EffiRpcCaller;
import io.effi.rpc.annotation.rpc.EffiRpcClient;
import io.effi.rpc.base.annotation.Body;
import io.effi.rpc.constant.Component;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@EffiRpcClient(
        remoteApplication = "provider",
        protocol = Component.Protocol.HTTP_1_1,
        clientConfig = "hello-client",
        path = "service",
        style = Component.AnnotationStyle.JAX_RS
)
public interface HelloClient {

    @GET
    @Path("hello")
    String hello(@QueryParam("name") String name, @QueryParam("age") Integer age);

    @POST
    @Path("helloList")
    @EffiRpcCaller(path = "helloList", protocol = Component.Protocol.HTTP_1_1, style = Component.AnnotationStyle.JAX_RS)
    List<ParentObject> helloList(@QueryParam("name") String name,
                                 @HeaderParam("content-type11") String contentType,
                                 @Body List<ParentObject> list);

    @POST
    @Path("helloList")
    @EffiRpcCaller(path = "helloList", protocol = Component.Protocol.HTTP_2, style = Component.AnnotationStyle.JAX_RS, clientConfig = "h2-client")
    CompletableFuture<List<ParentObject>> helloListAsync(@QueryParam("name") String name,
                                                         @HeaderParam("content-type111") String contentType,
                                                         @Body List<ParentObject> list);
}
