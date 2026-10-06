package io.effi.rpc.spring;

import io.effi.rpc.annotation.rpc.Call;
import io.effi.rpc.annotation.rpc.CallGroup;
import io.effi.rpc.annotation.rpc.ServeGroup;
import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.context.Servant;
import io.effi.rpc.context.options.CallerOptions;
import io.effi.rpc.protocol.http.h1.Http1Protocol;
import io.effi.rpc.spring.autoconfigure.EffiRpcAutoConfiguration;
import io.effi.rpc.spring.bean.EffiRpcConsumerFactory;
import io.effi.rpc.spring.bean.EffiRpcConsumerRegistrar;
import io.effi.rpc.spring.bean.EffiRpcProviderExporter;
import io.effi.rpc.spring.bean.EffiRpcScopedComponentRegistrar;
import io.effi.rpc.spring.properties.EffiRpcProperties;
import io.effi.rpc.spring.support.EffiRpcInfrastructure;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EffiRpcSpringIntegrationTest {

    private static final AtomicInteger PLATFORM_IDS = new AtomicInteger();

    private static final AtomicBoolean LAZY_BEAN_INITIALIZED = new AtomicBoolean();

    @Test
    void autoConfigurationBacksOffWhenContextProvidesScopedBeans() {
        ScopedPlatform platform = new ScopedPlatform("context-platform-" + PLATFORM_IDS.incrementAndGet());
        ScopedApplication application = platform.newApplication("context-application");
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.registerBean(ScopedPlatform.class, () -> platform);
            context.registerBean(ScopedApplication.class, () -> application);
            context.register(AutoConfigurationTestConfiguration.class);
            context.refresh();

            assertSame(platform, context.getBean(ScopedPlatform.class));
            assertSame(application, context.getBean(ScopedApplication.class));
        } finally {
            application.close();
            platform.close();
        }
    }

    @Test
    void consumerFactoryUsesConfiguredDefaults() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", Map.of(
                    "effi.rpc.consumer.protocol", Http1Protocol.NAME,
                    "effi.rpc.consumer.locator", "direct"
            )));
            context.register(ConsumerConfiguration.class);
            context.refresh();

            EffiRpcConsumerFactory factory = context.getBean(EffiRpcConsumerFactory.class);
            TestConsumer consumer = factory.create(TestConsumer.class,
                    options -> options.addOption(CallerOptions.ENDPOINT, "127.0.0.1:1"));
            assertNotNull(consumer);
            assertNotEquals(TestConsumer.class, consumer.getClass());
        }
    }

    @Test
    void userConsumerBeanWinsOverAutoRegisteredProxy() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            AutoConfigurationPackages.register(
                    (BeanDefinitionRegistry) context.getBeanFactory(),
                    TestAnnotatedConsumer.class.getPackageName()
            );
            context.register(UserConsumerConfiguration.class);
            context.refresh();

            TestAnnotatedConsumer consumer = context.getBean(TestAnnotatedConsumer.class);
            assertEquals("user", consumer.hello());
        }
    }

    @Test
    void registersProviderAsServantFromAnnotation() {
        LAZY_BEAN_INITIALIZED.set(false);
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", Map.of(
                    "effi.rpc.provider.protocols[0]", Http1Protocol.NAME
            )));
            context.register(ProviderConfiguration.class);
            context.refresh();

            ScopedModule module = context.getBean(ScopedModule.class);
            assertEquals(1, module.componentCount(Servant.class));
            assertFalse(LAZY_BEAN_INITIALIZED.get());
        }
    }

    @Test
    void rejectsProviderModuleThatDoesNotExist() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", Map.of(
                    "effi.rpc.provider.protocols[0]", Http1Protocol.NAME,
                    "effi.rpc.provider.module", "missing-module"
            )));
            context.register(ProviderConfiguration.class);

            RuntimeException failure = assertThrows(RuntimeException.class, context::refresh);
            assertTrue(NestedExceptionUtils.getMostSpecificCause(failure).getMessage()
                    .contains("missing-module"));
        }
    }

    @Test
    void rejectsProviderRegistryThatDoesNotExist() {
        EffiRpcProperties properties = new Binder(new MapConfigurationPropertySource(Map.of(
                "effi.rpc.provider.registries[0]", "missing-registry"
        ))).bind("effi.rpc", Bindable.of(EffiRpcProperties.class)).get();
        ScopedPlatform platform = new ScopedPlatform("missing-registry-platform");
        try {
            IllegalStateException failure = assertThrows(IllegalStateException.class,
                    () -> EffiRpcInfrastructure.registerRegistries(platform, properties));
            assertTrue(failure.getMessage().contains("missing-registry"));
        } finally {
            platform.close();
        }
    }

    @Test
    void registersScopedComponentAfterContextsExist() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.register(ScopedComponentConfiguration.class);
            context.refresh();

            ScopedApplication application = context.getBean(ScopedApplication.class);
            assertEquals(1, application.componentCount(TestScopedComponent.class));
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
    static class ConsumerConfiguration {

        @Bean
        EffiRpcConsumerFactory effiRpcConsumerFactory(ScopedModule module, EffiRpcProperties properties) {
            return new EffiRpcConsumerFactory(module, properties);
        }

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
    @Import(EffiRpcConsumerRegistrar.class)
    static class UserConsumerConfiguration {

        @Bean
        ScopedPlatform platform() {
            return new ScopedPlatform("spring-user-consumer-" + PLATFORM_IDS.incrementAndGet());
        }

        @Bean
        ScopedApplication application(ScopedPlatform platform) {
            return platform.newApplication("user-consumer-application");
        }

        @Bean
        ScopedModule module(ScopedApplication application) {
            return application.defaultModule();
        }

        @Bean
        TestAnnotatedConsumer testAnnotatedConsumer() {
            return () -> "user";
        }
    }

    @Configuration(proxyBeanMethods = false)
    @Import(EffiRpcAutoConfiguration.class)
    static class AutoConfigurationTestConfiguration {
    }

    @Configuration(proxyBeanMethods = false)
    static class ScopedComponentConfiguration {

        @Bean
        static EffiRpcScopedComponentRegistrar effiRpcScopedComponentRegistrar(ApplicationContext context) {
            return new EffiRpcScopedComponentRegistrar(context);
        }

        @Bean
        TestScopedComponent testScopedComponent() {
            return new TestScopedComponent();
        }

        @Bean
        ScopedPlatform platform() {
            return new ScopedPlatform("scoped-component-platform");
        }

        @Bean
        ScopedApplication application(ScopedPlatform platform) {
            return platform.newApplication("scoped-component-application");
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
        EffiRpcProviderExporter effiRpcProviderExporter(
                ObjectProvider<EffiRpcProperties> properties,
                ObjectProvider<ScopedApplication> application
        ) {
            return new EffiRpcProviderExporter(properties, application);
        }

        @Bean
        TestProvider testProvider() {
            return new TestProvider();
        }

        @Bean
        @Lazy
        Object unrelatedLazyBean() {
            LAZY_BEAN_INITIALIZED.set(true);
            return new Object();
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

    @ServeGroup(interfaces = TestConsumer.class, protocol = {Http1Protocol.NAME})
    static class TestProvider implements TestConsumer {

        @Override
        public String hello(String name) {
            return "hello " + name;
        }
    }

    @ScopedComponent(scope = ScopedComponent.Scope.APPLICATION)
    static class TestScopedComponent {
    }
}
