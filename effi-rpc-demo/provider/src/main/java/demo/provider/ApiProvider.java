package demo.provider;

import io.effi.rpc.boot.ApplicationServiceRegistrar;
import io.effi.rpc.boot.ComplexRemoteService;
import io.effi.rpc.boot.ServerLauncher;
import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.registry.DefaultRegistryConfig;
import io.effi.rpc.component.registry.RegistryConfig;
import io.effi.rpc.config.DefaultHierarchicalConfig;
import io.effi.rpc.constant.Tags;
import io.effi.rpc.context.parameter.Header;
import io.effi.rpc.context.parameter.MethodMapper;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.protocol.http.arg.api.HttpMethodMapperBuilder;
import io.effi.rpc.protocol.http.h2.Http2Callee;
import io.effi.rpc.protocol.http.h2.Http2ServerConfig;

/**
 * @Author WenBo Zhou
 * @Date 2025/4/5 17:04
 */
public class ApiProvider {

    private static final Logger logger = LoggerFactory.getLogger(ApiProvider.class);

    public static void main(String[] args) {
        ScopedApplication application = ScopedApplication.defaultApplication().withName("provider");
        ScopedModule module = application.defaultModule();
        ComplexRemoteService<HelloService> remoteService = new ComplexRemoteService<>(new HelloService());
        MethodMapper<HelloService> methodMapper = new HttpMethodMapperBuilder<>(remoteService, "hello")
                .mappedParameterType(String.class, Header.source("content-type"))
                .mappedParameterType(Integer.class, Header.source("content-length"))
                .build();
        application.platform()
                .registry()
                .register(RegistryConfig.class, DefaultRegistryConfig.builder().authority("consul://127.0.0.1:8500").build().addTags(Tags.PROVIDER, Tags.FORCE_ACTIVE));
        ServerLauncher.allocate(application, Http2ServerConfig.defaultConfig(), 8090);
        Http2Callee.builder(methodMapper, new DefaultHierarchicalConfig())
                .path("hello")
                .module(module)
                .build();
        ApplicationServiceRegistrar serviceRegistrar = new ApplicationServiceRegistrar(application);
        serviceRegistrar.register();
    }
}
