package demo.provider;

import demo.provider.model.ParentObject;
import io.effi.rpc.annotation.rpc.Serve;
import io.effi.rpc.annotation.rpc.ServeGroup;
import io.effi.rpc.context.annotation.Body;
import io.effi.rpc.protocol.http.arg.annotation.jax.JaxRsStyleResolver;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import java.util.List;

@ServeGroup
public class HelloService extends CallLogInterceptor {

    public String hello(String name) {
        Chain chain = null;
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
    @Serve(path = "helloList", style = JaxRsStyleResolver.NAME)
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
