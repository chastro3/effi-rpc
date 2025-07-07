package io.effi.rpc.spring;

import io.effi.rpc.boot.EffiRpcBootstrap;
import io.effi.rpc.component.EffiRpcApplication;
import io.effi.rpc.component.EffiRpcModule;
import io.effi.rpc.component.EffiRpcPlatform;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EffiRpcAutoConfiguration {

    @Bean
    public EffiRpcPlatform effiRpcPlatform() {
        return EffiRpcPlatform.getInstance();
    }

    @Bean
    @ConditionalOnMissingBean
    public EffiRpcApplication effiRpcApplication(EffiRpcPlatform platform, ApplicationContext context) {
        String applicationName = context.getApplicationName();
        return platform.newApplication(applicationName);
    }

    @Bean
    @ConditionalOnMissingBean
    public EffiRpcModule defaultEffiRpcModule(EffiRpcApplication application) {
        return application.defaultModule();
    }

    @Bean
    @ConditionalOnBean(EffiRpcApplication.class)
    public EffiRpcBootstrap effiRpcBootstrap(EffiRpcApplication application) {
        return EffiRpcBootstrap.newInstance(application);
    }
}
