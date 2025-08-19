package io.effi.rpc.processor;

import io.effi.rpc.nativetools.NativeConfig;
import io.effi.rpc.nativetools.ReflectConfig;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

/**
 * Handles the {@link NativeConfig.Reflect} annotation.
 */
public class NativeReflectConfigHandler extends AnnotationHandler<NativeConfig.Reflect> {

    public NativeReflectConfigHandler(ProcessingEnvironment processingEnv) {
        super(NativeConfig.Reflect.class, processingEnv);
    }

    @Override
    protected void handle(Set<? extends Element> elements, RoundEnvironment roundEnv) {
        ReflectConfigResourceSection reflectConfigResourceSection = resourceSection(ReflectConfigResourceSection.class);
        for (Element element : elements) {
            if (element instanceof TypeElement typeElement) {
                NativeConfig.Reflect reflect = typeElement.getAnnotation(NativeConfig.Reflect.class);
                ReflectConfig.Item item = ReflectConfig.Item.from(reflect, helper())
                        .type(helper().qualifiedNameOf(typeElement));
                reflectConfigResourceSection.nativeConfig().addItem(item);
            }
        }
    }
}
