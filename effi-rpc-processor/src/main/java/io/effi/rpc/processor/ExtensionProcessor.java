package io.effi.rpc.processor;

import io.effi.rpc.common.spi.Extensible;
import io.effi.rpc.common.spi.Extension;
import io.effi.rpc.common.util.CollectionUtil;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ExtensionProcessor extends BaseProcessor<Extension> {

    public ExtensionProcessor(ResourceCollector resourceCollector) {
        super(Extension.class, resourceCollector);
    }

    @Override
    protected void process(Set<? extends Element> elements, RoundEnvironment roundEnv) {
        for (Element element : elements) {
            if (element instanceof TypeElement typeElement) {
                // Get @Extension
                AnnotationMirror extensionMirror = helper.getAnnotationMirror(typeElement, Extension.class);
                // Get interfaces from @Extension#interfaces()
                List<Name> supportedInterfaces = getSupportedInterfaces(extensionMirror);
                // Get need generate interfaces.
                Set<Name> neededInterfaces = helper.getAllInterfaceNames(typeElement, item -> isSupportedInterface(item, supportedInterfaces));
                for (Name interfaceName : neededInterfaces) {
                    String extensionName = helper.getQualifiedClassName(typeElement);
                    String interfaceNameStr = interfaceName.toString();
                    resourceCollector.addExtension(interfaceNameStr, extensionName);
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
                    result.add(helper.asType(mirror).getQualifiedName());
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
