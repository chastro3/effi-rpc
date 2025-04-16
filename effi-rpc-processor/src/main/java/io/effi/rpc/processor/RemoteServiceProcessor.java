package io.effi.rpc.processor;

import io.effi.rpc.common.compile.DynamicAccessor;
import io.effi.rpc.contract.annotation.EffiRpcService;
import io.effi.rpc.nativetools.ReflectConfigItem;
import org.objectweb.asm.Opcodes;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

public class RemoteServiceProcessor extends BaseProcessor<EffiRpcService> implements Opcodes {

    public RemoteServiceProcessor(ResourceCollector resourceCollector) {
        super(EffiRpcService.class, resourceCollector);
    }

    @Override
    protected void process(Set<? extends Element> elements, RoundEnvironment roundEnv) {
        for (Element element : elements) {
            if (element instanceof TypeElement typeElement) {
                String className = helper.getQualifiedClassName(typeElement);
                ReflectConfigItem serviceItem = new ReflectConfigItem()
                        .className(className)
                        .queryAllDeclaredMethods(true)
                        .queryAllPublicMethods(true);
                ReflectConfigItem serviceAccessItem = new ReflectConfigItem()
                        .className(className + DynamicAccessor.SUFFIX)
                        .addMethod("<init>", null);
                resourceCollector.addRemoteService(typeElement);
                resourceCollector.addReflectConfigItem(serviceItem);
                resourceCollector.addReflectConfigItem(serviceAccessItem);
            }
        }
    }
}
