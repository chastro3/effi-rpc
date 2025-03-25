package demo.consumer;

import demo.consumer.model.ParentObject;
import io.effi.rpc.common.constant.Component;
import io.effi.rpc.contract.annotation.Body;
import io.effi.rpc.contract.annotation.EffiRpcCaller;
import io.effi.rpc.contract.annotation.EffiRpcClient;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@EffiRpcClient(application = "provider", protocol = "h2", path = "service", clientConfig = "hello-client")
public interface HelloClient {

    @POST
    @Path("http-helloList")
    @EffiRpcCaller(path = "helloList", style = Component.AnnotationStyle.JAX_RS)
    List<ParentObject> helloList(@QueryParam("name") String name,
                                 @HeaderParam("content-type") String contentType,
                                 @Body List<ParentObject> list);

    @POST
    @Path("http-helloList")
    @EffiRpcCaller(path = "helloList", style = Component.AnnotationStyle.JAX_RS)
    CompletableFuture<List<ParentObject>> helloListAsync(@QueryParam("name") String name,
                                                         @HeaderParam("content-type") String contentType,
                                                         @Body List<ParentObject> list);
}
