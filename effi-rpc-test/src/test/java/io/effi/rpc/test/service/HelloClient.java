package io.effi.rpc.test.service;

import io.effi.rpc.annotation.rpc.EffiRpcCaller;
import io.effi.rpc.annotation.rpc.EffiRpcClient;
import io.effi.rpc.config.ConfigValues;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import static io.effi.rpc.config.ConfigValues.Protocol.HTTP_1_1;
import static io.effi.rpc.config.ConfigValues.Protocol.HTTP_2;

@EffiRpcClient(remoteApplication = "provider", protocol = HTTP_1_1, style = ConfigValues.AnnotationStyle.JAX_RS)
public interface HelloClient {

    @GET
    @Path("hello1")
    @EffiRpcCaller(path = "hello", protocol = HTTP_2)
    String hello(@QueryParam("name") String name, @QueryParam("age") int age);
}
