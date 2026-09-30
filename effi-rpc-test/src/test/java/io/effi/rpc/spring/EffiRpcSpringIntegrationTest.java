package io.effi.rpc.spring;

import io.effi.rpc.annotation.rpc.Call;
import io.effi.rpc.annotation.rpc.CallGroup;
import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.context.Servant;
import io.effi.rpc.protocol.http.h1.Http1Protocol;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EffiRpcSpringIntegrationTest {

    private static final AtomicInteger PLATFORM_IDS = new AtomicInteger();

    @Test
    void registersConsumerProxyFromAnnotation() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", Map.of(
                    "effi.rpc.consumer.common.protocol", Http1Protocol.NAME,
                    "effi.rpc.consumer.common.locator", "direct",
                    "effi.rpc.consumer.targets.test-consumer.interfaces[0]", TestConsumer.class.getName(),
                    "effi.rpc.consumer.targets.test-consumer.endpoint", "127.0.0.1:1"
            )));
            context.register(ConsumerConfiguration.class);
            context.refresh();

            TestConsumer consumer = context.getBean(TestConsumer.class);
            assertNotNull(consumer);
            assertNotEquals(TestConsumer.class, consumer.getClass());
        }
    }

    @Test
    void registersProviderAsServantFromAnnotation() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", Map.of(
                    "effi.rpc.provider.common.protocols[0]", Http1Protocol.NAME
            )));
            context.register(ProviderConfiguration.class);
            context.refresh();

            ScopedModule module = context.getBean(ScopedModule.class);
            assertEquals(1, module.componentCount(Servant.class));
        }
    }

    @Test
    void scansAnnotatedConsumerWithoutExplicitTargetConfiguration() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            AutoConfigurationPackages.register(
                    (BeanDefinitionRegistry) context.getBeanFactory(),
                    TestAnnotatedConsumer.class.getPackageName()
            );
            context.register(AnnotatedConsumerConfiguration.class);
            context.refresh();

            assertNotNull(context.getBean(TestAnnotatedConsumer.class));
        }
    }

    @Configuration
    @EnableConfigurationProperties(EffiRpcProperties.class)
    @Import(EffiRpcConsumerRegistrar.class)
    static class ConsumerConfiguration {

        @Bean
        ScopedPlatform platform() {
            return new ScopedPlatform("spring-consumer-" + PLATFORM_IDS.incrementAndGet());
        }

        @Bean
        ScopedApplication application(ScopedPlatform platform) {
            return platform.newApplication("consumer-application");
        }

        @Bean
        ScopedModule module(ScopedApplication application) {
            return application.defaultModule();
        }

    }

    @Configuration
    @EnableConfigurationProperties(EffiRpcProperties.class)
    static class ProviderConfiguration {

        @Bean
        ScopedPlatform platform() {
            return new ScopedPlatform("spring-provider-" + PLATFORM_IDS.incrementAndGet());
        }

        @Bean
        ScopedApplication application(ScopedPlatform platform) {
            return platform.newApplication("provider-application");
        }

        @Bean
        ScopedModule module(ScopedApplication application) {
            return application.defaultModule();
        }

        @Bean
        EffiRpcProviderExporter effiRpcProviderExporter() {
            return new EffiRpcProviderExporter();
        }

        @Bean
        TestProvider testProvider() {
            return new TestProvider();
        }
    }

    @Configuration
    @Import(EffiRpcConsumerRegistrar.class)
    static class AnnotatedConsumerConfiguration {

        @Bean
        ScopedPlatform platform() {
            return new ScopedPlatform("spring-annotated-consumer-" + PLATFORM_IDS.incrementAndGet());
        }

        @Bean
        ScopedApplication application(ScopedPlatform platform) {
            return platform.newApplication("annotated-consumer-application");
        }

        @Bean
        ScopedModule module(ScopedApplication application) {
            return application.defaultModule();
        }
    }

    interface TestConsumer {
        String hello(String name);
    }

    @CallGroup
    interface TestAnnotatedConsumer {

        @Call(
                path = "/hello",
                protocol = Http1Protocol.NAME,
                locator = "direct",
                endpoint = "127.0.0.1:1"
        )
        String hello();
    }

    @EffiRpcService(interfaces = TestConsumer.class, protocols = Http1Protocol.NAME)
    static class TestProvider implements TestConsumer {

        @Override
        public String hello(String name) {
            return "hello " + name;
        }
    }
}
