package demo.provider;

import io.effi.rpc.component.EffiRpcPlatform;
import io.effi.rpc.config.HierarchicalNodeConfig;
import io.effi.rpc.component.EffiRpcApplication;
import io.effi.rpc.component.EffiRpcModule;
import io.effi.rpc.base.parameter.Header;
import io.effi.rpc.base.parameter.MethodMapper;
import io.effi.rpc.boot.ComplexRemoteService;
import io.effi.rpc.boot.DefaultServiceHost;
import io.effi.rpc.protocol.http.arg.api.HttpMethodMapperBuilder;
import io.effi.rpc.protocol.http.h2.Http2Callee;
import io.effi.rpc.protocol.http.h2.Http2ServerConfig;

/**
 * @Author WenBo Zhou
 * @Date 2025/4/5 17:04
 */
public class ApiProvider {

    public static void main(String[] args) {
        EffiRpcApplication application = EffiRpcPlatform.init("effi-rpc-platform")
                .newApplication("provider");
        EffiRpcModule module = application.defaultModule();
        ComplexRemoteService<HelloService> remoteService = new ComplexRemoteService<>(new HelloService());
        MethodMapper<HelloService> methodMapper = new HttpMethodMapperBuilder<>(remoteService, "hello")
                .mappedParameterType(String.class, Header.source("content-type"))
                .mappedParameterType(Integer.class, null)
                .build();

        DefaultServiceHost exporter = DefaultServiceHost.builder()
                .exportedPort(8090)
                .serverConfig(Http2ServerConfig.defaultConfig())
                .build();

        Http2Callee.builder(methodMapper, new HierarchicalNodeConfig())
                .path("hello")
                .module(module)
                .build();

        exporter.start();

    }
}
