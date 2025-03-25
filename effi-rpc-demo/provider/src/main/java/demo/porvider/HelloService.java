package demo.porvider;

import demo.porvider.model.ParentObject;
import io.effi.rpc.common.constant.Component;
import io.effi.rpc.contract.annotation.Body;
import io.effi.rpc.contract.annotation.EffiRpcCallee;
import io.effi.rpc.contract.annotation.EffiRpcService;
import jakarta.ws.rs.*;

import java.util.List;

@EffiRpcService(
        path = "service",
        value = "hello",
        protocol = {"http", "h2"},
        excludedPort = {8080, 8081},
        modules = {"module1", "module2"},
        filters = {"filter1", "filter2"},
        desc = "hello service",
        serialization = "json",
        compression = "gzip",
        serializationThreshold = 1024,
        deserializationThreshold = 1024,
        threadPool = "threadPool"
)
public class HelloService {

    public String hello(String name) {
        return "Hello " + name;
    }

    @GET
    @Path("hello")
    public String hello(@QueryParam("name") String name, @QueryParam("age") int age) {
        return "hello " + name + ", age:" + age;
    }

    @POST
    @Path("http-helloList")
    @EffiRpcCallee(path = "helloList", style = Component.AnnotationStyle.JAX_RS)
    public List<ParentObject> helloList(@QueryParam("name") String name,
                                        @HeaderParam("content-type") String contentType,
                                        @Body List<ParentObject> list) {
        return ParentObject.getObjList("provider list");
    }

}
