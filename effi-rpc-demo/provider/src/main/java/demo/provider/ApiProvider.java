package demo.provider;

import io.effi.rpc.boot.ApplicationServiceRegistrar;
import io.effi.rpc.boot.ComplexServantGroup;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.registry.DefaultRegistryConfig;
import io.effi.rpc.context.Servant;
import io.effi.rpc.context.parameter.Header;
import io.effi.rpc.context.parameter.ServantMethod;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.protocol.http.arg.api.HttpServantMethodBuilder;
import io.effi.rpc.protocol.http.h2.Http2Servant;
import io.effi.rpc.protocol.http.h2.Http2ServerConfig;

/**
 * @Author WenBo Zhou
 * @Date 2025/4/5 17:04
 */
public class ApiProvider {

    private static final Logger logger = LoggerFactory.getLogger(ApiProvider.class);

    public static void main(String[] args) {
        ScopedModule module = ScopedModule.defaultInstance();
        Servant servant = createServant(module);
        ApplicationServiceRegistrar.forApplication(module.application())
                .attachServer(Http2ServerConfig.defaultConfig(), 8090)
                .registry(DefaultRegistryConfig.builder().authority("consul://127.0.0.1:8500").build())
                .register();
    }

    private static Servant createServant(ScopedModule module) {
        ComplexServantGroup<HelloService> group = new ComplexServantGroup<>(new HelloService());
        ServantMethod<HelloService> servantMethod = new HttpServantMethodBuilder<>(group, "hello")
                .mappedParameterType(String.class, Header.source("content-type"))
                .mappedParameterType(Integer.class, Header.source("content-length"))
                .build();
        return Http2Servant.builder(servantMethod)
                .path("hello")
                .module(module)
                .build();
    }
}
