package io.effi.rpc.spring;

import io.effi.rpc.boot.EffiRpcBootstrap;
import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.util.StringUtil;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

@AutoConfiguration
@ConditionalOnProperty(prefix = "effi.rpc", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(EffiRpcProperties.class)
@Import(EffiRpcConsumerRegistrar.class)
public class EffiRpcAutoConfiguration {

    @Bean
    public static EffiRpcComponentBeanPostProcessor effiRpcComponentBeanPostProcessor() {
        return new EffiRpcComponentBeanPostProcessor();
    }

    @Bean
    @ConditionalOnMissingBean
    public ScopedPlatform effiRpcPlatform() {
        return ScopedPlatform.defaultInstance().name("spring-platform");
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
        return platform.defaultApplication().name(applicationName);
    }

    @Bean
    @ConditionalOnMissingBean
    public ScopedModule defaultEffiRpcModule(ScopedApplication application) {
        return application.defaultModule();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ScopedApplication.class)
    public EffiRpcBootstrap effiRpcBootstrap(ScopedApplication application) {
        return EffiRpcBootstrap.newInstance(application);
    }

    @Bean
    @ConditionalOnMissingBean
    public EffiRpcProviderExporter effiRpcProviderExporter() {
        return new EffiRpcProviderExporter();
    }

    @Bean
    @ConditionalOnMissingBean
    public EffiRpcInfrastructureInitializer effiRpcInfrastructureInitializer() {
        return new EffiRpcInfrastructureInitializer();
    }

    @Bean
    @ConditionalOnMissingBean
    public EffiRpcApplicationStarter effiRpcApplicationStarter(EffiRpcBootstrap bootstrap) {
        return new EffiRpcApplicationStarter(bootstrap);
    }
}
