package io.effi.rpc.test.service;

import io.effi.rpc.annotation.rpc.ServeGroup;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

@ServeGroup(module = "test")
public class HelloService {

    public String hello(String name) {
        return "hello " + name;
    }

    @GET
    @Path("hello")
    public String hello(@QueryParam("name") String name, @QueryParam("age") int age) {
        return "hello " + name + ", age:" + age;
    }
}
