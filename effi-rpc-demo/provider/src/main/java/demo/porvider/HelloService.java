package demo.porvider;

import demo.porvider.model.ParentObject;
import io.effi.rpc.common.constant.Component;
import io.effi.rpc.contract.annotation.Body;
import io.effi.rpc.contract.annotation.EffiRpcCallee;
import io.effi.rpc.contract.annotation.EffiRpcService;
import jakarta.ws.rs.*;

import java.util.List;

@EffiRpcService(protocol = "http,h2",style = Component.AnnotationStyle.JAX_RS,path = "service")
public class HelloService {

    public String hello(String name) {
        return "Hello " + name;
    }

    @GET
    @Path("hello")
    public String hello(@QueryParam("name") String name, @QueryParam("age") Integer age) {
        return "hello " + name + ", age:" + age;
    }

    @POST
    @Path("helloList")
    @EffiRpcCallee(path = "helloList", style = Component.AnnotationStyle.JAX_RS)
    public List<ParentObject> helloList(@QueryParam("name") String name,
                                        @HeaderParam("content-type") String contentType,
                                        @Body List<ParentObject> list) {
        return ParentObject.getObjList("provider list");
    }

}
