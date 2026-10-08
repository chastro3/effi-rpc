package io.effi.rpc.spring.autoconfigure;

import io.effi.rpc.annotation.rpc.CallGroup;
import io.effi.rpc.annotation.rpc.ServeGroup;
import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.core.EffiRpcBootstrap;
import io.effi.rpc.spring.consumer.CallGroupRegistrar;
import io.effi.rpc.spring.consumer.InterfaceCallGroupFactory;
import io.effi.rpc.spring.provider.ServeGroupRegistrar;
import io.effi.rpc.spring.support.EffiRpcApplicationLifecycle;
import io.effi.rpc.spring.support.InfrastructureConfigurer;
import io.effi.rpc.spring.support.ScopedComponentRegistrar;
import io.effi.rpc.util.StringUtil;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

/**
 * Provides Spring Boot auto-configuration for Effi RPC.
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "effi.rpc", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(EffiRpcProperties.class)
public class EffiRpcAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public static ScopedComponentRegistrar effiRpcScopedComponentRegistrar(ApplicationContext context) {
        return new ScopedComponentRegistrar(context);
    }

    @Bean
    @ConditionalOnMissingBean
    public ScopedPlatform effiRpcPlatform(EffiRpcProperties properties) {
        ScopedPlatform platform = ScopedPlatform.defaultInstance();
        InfrastructureConfigurer.registerRegistries(platform, properties);
        return platform;
    }

    @Bean
    @ConditionalOnMissingBean
    public ScopedApplication effiRpcApplication(ScopedPlatform platform, ApplicationContext context, Environment environment, EffiRpcProperties properties) {
        String applicationName = StringUtil.isBlankOrDefault(
                properties.application().name(),
                environment.getProperty("spring.application.name")
        );
        applicationName = StringUtil.isBlankOrDefault(applicationName, context.getApplicationName());
        applicationName = StringUtil.isBlankOrDefault(applicationName, "default");
        ScopedApplication application = platform.defaultApplication().name(applicationName);
        InfrastructureConfigurer.attachServers(application, properties);
        return application;
    }

    @Bean
    @ConditionalOnMissingBean
    public ScopedModule defaultEffiRpcModule(ScopedApplication application) {
        return application.defaultModule();
    }

    @Bean
    @ConditionalOnMissingBean
    public InterfaceCallGroupFactory interfaceCallGroupFactory(ScopedModule module, EffiRpcProperties properties) {
        return new InterfaceCallGroupFactory(module, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ScopedApplication.class)
    public EffiRpcBootstrap effiRpcBootstrap(ScopedApplication application) {
        return EffiRpcBootstrap.newInstance(application);
    }

    @Bean
    @ConditionalOnMissingBean
    public EffiRpcApplicationLifecycle effiRpcApplicationLifecycle(EffiRpcBootstrap bootstrap) {
        return new EffiRpcApplicationLifecycle(bootstrap);
    }

    /**
     * Provides consumer-side auto-configuration.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(CallGroup.class)
    @Import(CallGroupRegistrar.class)
    public static class ConsumerAutoConfiguration {
    }

    /**
     * Provides provider-side auto-configuration.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(ServeGroup.class)
    public static class ProviderAutoConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public static ServeGroupRegistrar serveGroupRegistrar(
                ObjectProvider<EffiRpcProperties> properties,
                ObjectProvider<ScopedApplication> application
        ) {
            return new ServeGroupRegistrar(properties, application);
        }
    }
}
