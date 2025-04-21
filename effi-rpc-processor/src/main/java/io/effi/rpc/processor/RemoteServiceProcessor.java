package io.effi.rpc.processor;

import io.effi.rpc.common.compile.DynamicAccessor;
import io.effi.rpc.contract.annotation.EffiRpcService;
import io.effi.rpc.nativetools.ConditionItem;
import io.effi.rpc.nativetools.ReflectConfigItem;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

public class RemoteServiceProcessor extends BaseProcessor<EffiRpcService> {

    public RemoteServiceProcessor(ResourceCollector resourceCollector) {
        super(EffiRpcService.class, resourceCollector);
    }

    @Override
    protected void process(Set<? extends Element> elements, RoundEnvironment roundEnv) {
        for (Element element : elements) {
            if (element instanceof TypeElement typeElement) {
                String className = helper.getQualifiedClassName(typeElement);
                ReflectConfigItem serviceItem = new ReflectConfigItem()
                        .name(className)
                        .queryAllDeclaredMethods(true)
                        .queryAllPublicMethods(true);
                ReflectConfigItem serviceAccessItem = new ReflectConfigItem()
                        .condition(new ConditionItem().typeReachable(className))
                        .name(className + DynamicAccessor.SUFFIX)
                        .method("<init>", null);
                resourceCollector.addRemoteService(typeElement);
                resourceCollector.addReflectConfigItem(serviceItem);
                resourceCollector.addReflectConfigItem(serviceAccessItem);
            }
        }
    }
}
