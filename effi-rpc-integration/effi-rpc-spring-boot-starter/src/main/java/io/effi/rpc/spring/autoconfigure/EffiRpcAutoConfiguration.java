package io.effi.rpc.spring.autoconfigure;

import io.effi.rpc.annotation.rpc.CallGroup;
import io.effi.rpc.annotation.rpc.ServeGroup;
import io.effi.rpc.boot.EffiRpcBootstrap;
import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.spring.bean.EffiRpcConsumerFactory;
import io.effi.rpc.spring.bean.EffiRpcConsumerRegistrar;
import io.effi.rpc.spring.bean.EffiRpcProviderExporter;
import io.effi.rpc.spring.bean.EffiRpcScopedComponentRegistrar;
import io.effi.rpc.spring.lifecycle.EffiRpcApplicationLifecycle;
import io.effi.rpc.spring.properties.EffiRpcProperties;
import io.effi.rpc.spring.support.EffiRpcInfrastructure;
import io.effi.rpc.util.StringUtil;
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
import org.springframework.beans.factory.ObjectProvider;

@AutoConfiguration
@ConditionalOnProperty(prefix = "effi.rpc", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(EffiRpcProperties.class)
public class EffiRpcAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public static EffiRpcScopedComponentRegistrar effiRpcScopedComponentRegistrar(ApplicationContext context) {
        return new EffiRpcScopedComponentRegistrar(context);
    }

    @Bean
    @ConditionalOnMissingBean
    public ScopedPlatform effiRpcPlatform(EffiRpcProperties properties) {
        ScopedPlatform platform = ScopedPlatform.defaultInstance();
        EffiRpcInfrastructure.registerRegistries(platform, properties);
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
        EffiRpcInfrastructure.attachServers(application, properties);
        return application;
    }

    @Bean
    @ConditionalOnMissingBean
    public ScopedModule defaultEffiRpcModule(ScopedApplication application) {
        return application.defaultModule();
    }

    @Bean
    @ConditionalOnMissingBean
    public EffiRpcConsumerFactory effiRpcConsumerFactory(ScopedModule module, EffiRpcProperties properties) {
        return new EffiRpcConsumerFactory(module, properties);
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

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(CallGroup.class)
    @Import(EffiRpcConsumerRegistrar.class)
    public static class ConsumerAutoConfiguration {
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(ServeGroup.class)
    public static class ProviderAutoConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public static EffiRpcProviderExporter effiRpcProviderExporter(
                ObjectProvider<EffiRpcProperties> properties,
                ObjectProvider<ScopedApplication> application
        ) {
            return new EffiRpcProviderExporter(properties, application);
        }
    }
}
