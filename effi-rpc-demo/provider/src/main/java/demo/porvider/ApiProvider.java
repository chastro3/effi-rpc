package demo.porvider;

import io.effi.rpc.common.config.NodeConfig;
import io.effi.rpc.contract.module.EffRpcApplication;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.contract.parameter.Header;
import io.effi.rpc.contract.parameter.MethodMapper;
import io.effi.rpc.engine.ComplexRemoteService;
import io.effi.rpc.engine.DefaultServerExporter;
import io.effi.rpc.protocol.http.arg.api.HttpMethodMapperBuilder;
import io.effi.rpc.protocol.http.h2.Http2Callee;
import io.effi.rpc.protocol.http.h2.Http2ServerConfig;

/**
 * @Author WenBo Zhou
 * @Date 2025/4/5 17:04
 */
public class ApiProvider {

    public static void main(String[] args) {
        EffRpcApplication application = new EffRpcApplication("provider");
        EffiRpcModule module = application.defaultModule();
        ComplexRemoteService<HelloService> remoteService = new ComplexRemoteService<>(new HelloService());
        MethodMapper<HelloService> methodMapper = new HttpMethodMapperBuilder<>(remoteService, "hello")
                .mappedParameterType(String.class, Header.source("content-type"))
                .mappedParameterType(Integer.class, null)
                .build();

        DefaultServerExporter exporter = DefaultServerExporter.builder()
                .exportedPort(8090)
                .module(module)
                .serverConfig(Http2ServerConfig.defaultConfig())
                .build();

        Http2Callee.builder(methodMapper, new NodeConfig())
                .path("hello")
                .export(module)
                .build();

        exporter.export();

    }
}
