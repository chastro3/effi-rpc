package io.effi.rpc.test.service;

import io.effi.rpc.annotation.rpc.Call;
import io.effi.rpc.annotation.rpc.CallGroup;
import io.effi.rpc.protocol.http.h2.Http2Protocol;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

@CallGroup()
public interface HelloClient {

    @GET
    @Path("hello1")
    @Call(path = "hello", protocol = Http2Protocol.NAME)
    String hello(@QueryParam("name") String name, @QueryParam("age") int age);
}
