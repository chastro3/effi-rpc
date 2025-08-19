package io.effi.rpc.processor;

import io.effi.rpc.annotation.rpc.EffiRpcService;
import io.effi.rpc.compile.DynamicAccessor;
import io.effi.rpc.nativetools.ConditionItem;
import io.effi.rpc.nativetools.ReflectConfig;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

/**
 * Handles the {@link EffiRpcService} annotation.
 */
public class RemoteServiceHandler extends AnnotationHandler<EffiRpcService> {

    public RemoteServiceHandler(ProcessingEnvironment processingEnv) {
        super(EffiRpcService.class, processingEnv);
    }

    @Override
    protected void handle(Set<? extends Element> elements, RoundEnvironment roundEnv) {
        RemoteServiceResourceSection remoteServiceResourceSection = resourceSection(RemoteServiceResourceSection.class);
        ReflectConfigResourceSection reflectConfigResourceSection = resourceSection(ReflectConfigResourceSection.class);
        for (Element element : elements) {
            if (element instanceof TypeElement typeElement) {
                String className = helper().qualifiedNameOf(typeElement);
                ReflectConfig.Item serviceItem = new ReflectConfig.Item()
                        .type(className)
                        .queryAllDeclaredMethods(true)
                        .queryAllPublicMethods(true);
                ReflectConfig.Item serviceAccessItem = new ReflectConfig.Item()
                        .condition(new ConditionItem().typeReached(className))
                        .type(className + DynamicAccessor.SUFFIX)
                        .method("<init>", null);
                remoteServiceResourceSection.add(typeElement);
                reflectConfigResourceSection.nativeConfig()
                        .addItem(serviceItem)
                        .addItem(serviceAccessItem);
            }
        }
    }
}
