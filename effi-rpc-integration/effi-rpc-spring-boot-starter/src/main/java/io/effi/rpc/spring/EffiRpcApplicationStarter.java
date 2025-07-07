package io.effi.rpc.spring;


import io.effi.rpc.boot.EffiRpcBootstrap;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

public class EffiRpcApplicationStarter implements ApplicationListener<ContextRefreshedEvent> {
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        EffiRpcBootstrap bootstrap = applicationContext.getBean(EffiRpcBootstrap.class);
        bootstrap.start();
    }
}
