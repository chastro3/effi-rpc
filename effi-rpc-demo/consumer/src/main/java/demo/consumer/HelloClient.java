package demo.consumer;

import demo.consumer.model.ParentObject;
import io.effi.rpc.annotation.rpc.Call;
import io.effi.rpc.annotation.rpc.CallGroup;
import io.effi.rpc.context.annotation.Body;
import io.effi.rpc.protocol.http.arg.annotation.jax.JaxRsStyleResolver;
import io.effi.rpc.protocol.http.h1.Http1Protocol;
import io.effi.rpc.protocol.http.h2.Http2Protocol;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@CallGroup(
        call = @Call(endpoint = "provider")
)
public interface HelloClient {

    @GET
    @Path("hello")
    String hello(@QueryParam("name") String name, @QueryParam("age") Integer age);

    @POST
    @Path("helloList")
    @Call(path = "helloList", protocol = Http1Protocol.NAME, style = JaxRsStyleResolver.NAME)
    List<ParentObject> helloList(@QueryParam("name") String name,
                                 @HeaderParam("content-type11") String contentType,
                                 @Body List<ParentObject> list);

    @POST
    @Path("helloList")
    @Call(path = "helloList", protocol = Http2Protocol.NAME, style = JaxRsStyleResolver.NAME, clientConfig = "h2-client")
    CompletableFuture<List<ParentObject>> helloListAsync(@QueryParam("name") String name,
                                                         @HeaderParam("content-type111") String contentType,
                                                         @Body List<ParentObject> list);
}
