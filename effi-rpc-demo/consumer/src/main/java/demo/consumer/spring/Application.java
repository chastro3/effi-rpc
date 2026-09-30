package demo.consumer.spring;

import demo.api.InterfaceHelloService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner rpcCall(InterfaceHelloService client, ConfigurableApplicationContext context) {
        return args -> {
            try {
                System.out.println("RPC result: " + client.hello("spring-consumer", 25));
            } finally {
                context.close();
            }
        };
    }
}
