package io.effi.rpc.processor;

import io.effi.rpc.contract.annotation.EffiRpcClient;
import io.effi.rpc.nativetools.ProxyConfigItem;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

public class RemoteClientProcessor extends BaseProcessor<EffiRpcClient> {

    protected RemoteClientProcessor(ResourceCollector resourceCollector) {
        super(EffiRpcClient.class, resourceCollector);
    }

    @Override
    protected void process(Set<? extends Element> elements, RoundEnvironment roundEnv) {
        for (Element element : elements) {
            if (element instanceof TypeElement typeElement) {
                String interfaceName = helper.getQualifiedClassName(typeElement);
                resourceCollector.addProxyInterface(new ProxyConfigItem().addInterface(interfaceName));
            }
        }

    }
}
