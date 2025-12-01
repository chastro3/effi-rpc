package io.effi.rpc.spring;

import io.effi.rpc.boot.EffiRpcBootstrap;
import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.ScopedPlatform;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EffiRpcAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ScopedPlatform effiRpcPlatform() {
        return ScopedPlatform.defaultInstance().name("spring-platform");
    }

    @Bean
    @ConditionalOnMissingBean
    public ScopedApplication effiRpcApplication(ScopedPlatform platform, ApplicationContext context) {
        String applicationName = context.getApplicationName();
        return platform.newApplication(applicationName);
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
}
