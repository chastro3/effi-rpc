package io.effi.rpc.processor;

import io.effi.rpc.compile.DynamicAccessor;
import io.effi.rpc.contract.annotation.EffiRpcService;
import io.effi.rpc.nativetools.ConditionItem;
import io.effi.rpc.nativetools.ReflectConfigItem;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

/**
 * Handles the processing of the {@link EffiRpcService} annotation.
 */
public class RemoteServiceHandler extends AnnotationHandler<EffiRpcService> {

    public RemoteServiceHandler(ProcessingEnvironment processingEnv) {
        super(EffiRpcService.class, processingEnv);
    }

    @Override
    protected void handle(Set<? extends Element> elements, RoundEnvironment roundEnv) {
        RemoteServiceResourceSection remoteServiceResourceSection = getResourceSection(RemoteServiceResourceSection.class);
        ReflectConfigResourceSection reflectConfigResourceSection = getResourceSection(ReflectConfigResourceSection.class);
        for (Element element : elements) {
            if (element instanceof TypeElement typeElement) {
                String className = helper().getQualifiedClassName(typeElement);
                ReflectConfigItem serviceItem = new ReflectConfigItem()
                        .name(className)
                        .queryAllDeclaredMethods(true)
                        .queryAllPublicMethods(true);
                ReflectConfigItem serviceAccessItem = new ReflectConfigItem()
                        .condition(new ConditionItem().typeReachable(className))
                        .name(className + DynamicAccessor.SUFFIX)
                        .method("<init>", null);
                remoteServiceResourceSection.add(typeElement);
                reflectConfigResourceSection.nativeConfig()
                        .addItem(serviceItem)
                        .addItem(serviceAccessItem);
            }
        }
    }
}
