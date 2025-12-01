package demo.provider;

import io.effi.rpc.boot.EffiRpcBootstrap;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.registry.DefaultRegistryConfig;
import io.effi.rpc.component.transport.CertificateConfig;
import io.effi.rpc.component.transport.support.DefaultCertificateConfig;
import io.effi.rpc.constant.Tags;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.protocol.http.h1.Http1ServerConfig;
import io.effi.rpc.protocol.http.h2.Http2ServerConfig;

import java.util.Scanner;

public class Provider {

    private static final Logger logger = LoggerFactory.getLogger(Provider.class);

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                String command = scanner.nextLine().trim().toLowerCase();
                if (command.equals("start")) {
                    start();
                }
                if (command.equals("stop")) {
                    ScopedPlatform.defaultInstance().close();
                }
            }
        }
    }

    public static void start() {
        CertificateConfig certificateConfig = DefaultCertificateConfig.builder()
                .id("server-cert")
                .certChainPath("C:\\Users\\zhouwenbo\\Desktop\\rpc\\certs\\server-cert.pem")
                .privateKeyPath("C:\\Users\\zhouwenbo\\Desktop\\rpc\\certs\\server-private-key.pem")
                .trustCertPath("C:\\Users\\zhouwenbo\\Desktop\\rpc\\certs\\ca-cert.pem")
                .build();
        Http2ServerConfig http2ServerConfig = Http2ServerConfig.builder()
                .certificate(certificateConfig)
                .build();
        EffiRpcBootstrap bootstrap = EffiRpcBootstrap.newInstance("provider")
                .applyServer(http2ServerConfig, 8090)
                .applyServer(http2ServerConfig, 8090)
                .applyServer(Http1ServerConfig.defaultConfig(), 8091)
                .registry(DefaultRegistryConfig.builder().authority("consul://127.0.0.1:8500").build().addTags(Tags.PROVIDER, Tags.FORCE_ACTIVE))
                //.registry(DefaultRegistryConfig.builder().url("nacos://127.0.0.1:8848").tag(RegistryConfig.Tag.PROVIDER, RegistryConfig.Tag.FORCE_ACTIVE).build())
                .service(new HelloService())
                .start();
        System.out.println(bootstrap);
    }

}
