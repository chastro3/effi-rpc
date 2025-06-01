package io.effi.rpc.processor;

import io.effi.rpc.annotation.spi.Extensible;
import io.effi.rpc.annotation.spi.Extension;
import io.effi.rpc.nativetools.ConditionItem;
import io.effi.rpc.nativetools.ReflectConfigItem;
import io.effi.rpc.util.CollectionUtil;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Handles the processing of the {@link Extension} annotation.
 */
public class ExtensionHandler extends AnnotationHandler<Extension> {

    public ExtensionHandler(ProcessingEnvironment processingEnv) {
        super(Extension.class, processingEnv);
    }

    @Override
    protected void handle(Set<? extends Element> elements, RoundEnvironment roundEnv) {
        ExtensionResourceSection extensionResourceSection = getResourceSection(ExtensionResourceSection.class);
        ReflectConfigResourceSection reflectConfigResourceSection = getResourceSection(ReflectConfigResourceSection.class);
        for (Element element : elements) {
            if (element instanceof TypeElement typeElement) {
                String extensionName = helper().getQualifiedClassName(typeElement);
                // Get @Extension
                AnnotationMirror extensionMirror = helper().getAnnotationMirror(typeElement, Extension.class);
                // Get interfaces from @Extension#interfaces()
                List<Name> supportedInterfaces = getSupportedInterfaces(extensionMirror);
                // Get need generate interfaces.
                Set<Name> neededInterfaces = helper().getAllInterfaceNames(typeElement, item -> isSupportedInterface(item, supportedInterfaces));
                for (Name interfaceName : neededInterfaces) {
                    String interfaceNameStr = interfaceName.toString();
                    extensionResourceSection.add(interfaceNameStr, extensionName);
                    ReflectConfigItem reflectConfigItem = new ReflectConfigItem()
                            .condition(new ConditionItem().typeReachable(interfaceNameStr))
                            .name(extensionName)
                            .method("<init>", null);
                    reflectConfigResourceSection.nativeConfig().addItem(reflectConfigItem);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<Name> getSupportedInterfaces(AnnotationMirror annotationMirror) {
        Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = annotationMirror.getElementValues();
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : elementValues.entrySet()) {
            ExecutableElement key = entry.getKey();
            if (key.getSimpleName().contentEquals("interfaces")) {
                List<? extends AnnotationValue> values = (List<? extends AnnotationValue>) entry.getValue().getValue();
                List<Name> result = new ArrayList<>(values.size());
                for (AnnotationValue value : values) {
                    TypeMirror mirror = (TypeMirror) value.getValue();
                    result.add(helper().asType(mirror).getQualifiedName());
                }
                return result;
            }
        }
        return null;
    }

    private boolean isSupportedInterface(TypeElement typeElement, List<Name> supportedInterfaces) {
        boolean hasExtensible = typeElement.getAnnotation(Extensible.class) != null;
        if (CollectionUtil.isEmpty(supportedInterfaces)) return hasExtensible;
        return supportedInterfaces.contains(typeElement.getQualifiedName()) && hasExtensible;
    }
}
