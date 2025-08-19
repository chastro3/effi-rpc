package io.effi.rpc.processor;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.nativetools.ConditionItem;
import io.effi.rpc.nativetools.ReflectConfig;
import io.effi.rpc.util.CollectionUtil;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Set;

/**
 * Handles the {@link Extension} annotation.
 */
public class ExtensionHandler extends AnnotationHandler<Extension> {

    public ExtensionHandler(ProcessingEnvironment processingEnv) {
        super(Extension.class, processingEnv);
    }

    @Override
    protected void handle(Set<? extends Element> elements, RoundEnvironment roundEnv) {
        ExtensionResourceSection extensionResourceSection = resourceSection(ExtensionResourceSection.class);
        ReflectConfigResourceSection reflectConfigResourceSection = resourceSection(ReflectConfigResourceSection.class);
        for (Element element : elements) {
            if (element instanceof TypeElement typeElement) {
                String extensionName = helper().qualifiedNameOf(typeElement);
                // Get interfaces from @Extension#interfaces()
                List<String> supportedInterfaces = helper().extractClassNames(typeElement, Extension.class, "interfaces");
                // Get need generate interfaces.
                Set<String> neededInterfaces = helper().findAllInterfaceNames(typeElement,
                        item -> isSupportedInterface(item, supportedInterfaces));
                for (String interfaceName : neededInterfaces) {
                    extensionResourceSection.add(interfaceName, extensionName);
                    ReflectConfig.Item reflectConfigItem = new ReflectConfig.Item()
                            .condition(new ConditionItem().typeReached(interfaceName))
                            .type(extensionName)
                            .method("<init>", null);
                    reflectConfigResourceSection.nativeConfig().addItem(reflectConfigItem);
                }
            }
        }
    }

    private boolean isSupportedInterface(TypeElement typeElement, List<String> supportedInterfaces) {
        Extensible extensible = typeElement.getAnnotation(Extensible.class);
        boolean hasExtensible = extensible != null;
        if (CollectionUtil.isEmpty(supportedInterfaces)) return hasExtensible;
        String className = helper().qualifiedNameOf(typeElement);
        return supportedInterfaces.contains(className) && hasExtensible;
    }
}
