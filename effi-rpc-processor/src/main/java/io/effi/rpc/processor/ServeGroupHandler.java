package io.effi.rpc.processor;

import io.effi.rpc.annotation.rpc.ServeGroup;
import io.effi.rpc.compile.DynamicAccessor;
import io.effi.rpc.nativetools.ConditionItem;
import io.effi.rpc.nativetools.ReflectConfig;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

/**
 * Handles the {@link ServeGroup} annotation.
 */
public class ServeGroupHandler extends AnnotationHandler<ServeGroup> {

    public ServeGroupHandler(ProcessingEnvironment processingEnv) {
        super(ServeGroup.class, processingEnv);
    }

    @Override
    protected void handle(Set<? extends Element> elements, RoundEnvironment roundEnv) {
        ServeGroupResourceSection serveGroupResourceSection = resourceSection(ServeGroupResourceSection.class);
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
                serveGroupResourceSection.add(typeElement);
                reflectConfigResourceSection.nativeConfig()
                        .addItem(serviceItem)
                        .addItem(serviceAccessItem);
            }
        }
    }
}
