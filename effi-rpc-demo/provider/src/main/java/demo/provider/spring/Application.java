package demo.provider.spring;

import io.effi.rpc.component.EffiRpcModule;
import io.effi.rpc.config.transport.ClientConfig;
import io.effi.rpc.protocol.http.h2.Http2ClientConfig;
import io.effi.rpc.spring.EnableEffiRpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * @Author WenBo Zhou
 * @Date 2025/6/29 21:40
 */
@EnableEffiRpc
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        EffiRpcModule module = context.getBean(EffiRpcModule.class);
        System.out.println(module);
    }

    @Bean
    public ClientConfig cusHttp2ClientConfig() {
        return Http2ClientConfig.builder()
                .ssl(false)
                .build();
    }
}
