package io.effi.rpc.test.service;

import io.effi.rpc.constant.Component;
import io.effi.rpc.contract.annotation.EffiRpcCaller;
import io.effi.rpc.contract.annotation.EffiRpcClient;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

@EffiRpcClient(application = "provider", protocol = "http", style = Component.AnnotationStyle.JAX_RS)
public interface HelloClient {

    @GET
    @Path("hello1")
    @EffiRpcCaller(path = "hello", protocol = "h2")
    String hello(@QueryParam("name") String name, @QueryParam("age") int age);
}
