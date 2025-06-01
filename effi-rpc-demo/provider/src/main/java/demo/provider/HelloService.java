package demo.provider;

import demo.provider.model.ParentObject;
import io.effi.rpc.constant.Component;
import io.effi.rpc.base.annotation.Body;
import io.effi.rpc.annotation.rpc.EffiRpcCallee;
import io.effi.rpc.annotation.rpc.EffiRpcService;
import jakarta.ws.rs.*;

import java.util.List;

import static io.effi.rpc.constant.Component.Protocol.HTTP_1_1;
import static io.effi.rpc.constant.Component.Protocol.HTTP_2;

@EffiRpcService(protocol = {HTTP_1_1, HTTP_2}, style = Component.AnnotationStyle.JAX_RS, path = "service")
public class HelloService extends CalleeLogFilter{

    public String hello(String name) {
        return "Hello " + name;
    }

    private int i = 0;
    @GET
    @Path("hello")
    public String hello(@QueryParam("name") String name, @QueryParam("age") Integer age) {
        return "hello " + name + ", age:" + age;
    }


    @Override
    public String toString() {
        return "哈哈哈哈";
    }

    @POST
    @Path("helloList")
    @EffiRpcCallee(path = "helloList", style = Component.AnnotationStyle.JAX_RS)
    public List<ParentObject> helloList(@QueryParam("name") String name,
                                        @HeaderParam("content-type") String contentType,
                                        @Body List<ParentObject> list) {
//        System.out.println("i:-------" + i++);
//        if (i < 3) {
//            int i = 1 / 0;
//        }
        return ParentObject.getObjList("provider list");
    }

    public static void helloStatic(String name) {
        System.out.println("helloStatic " + name);
    }

}
