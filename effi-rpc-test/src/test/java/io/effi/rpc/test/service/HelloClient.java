package io.effi.rpc.test.service;

import io.effi.rpc.annotation.rpc.Call;
import io.effi.rpc.annotation.rpc.CallGroup;
import io.effi.rpc.protocol.http.arg.annotation.jax.JaxRsStyleResolver;
import io.effi.rpc.protocol.http.h2.Http2Protocol;
import io.effi.rpc.proxy.jdk.JDKProxyFactory;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

@CallGroup(proxy = JDKProxyFactory.NAME)
public interface HelloClient {

    @GET
    @Path("hello1")
    @Call(path = "hello", protocol = Http2Protocol.NAME, style = JaxRsStyleResolver.NAME)
    String hello(@QueryParam("name") String name, @QueryParam("age") int age);
}
