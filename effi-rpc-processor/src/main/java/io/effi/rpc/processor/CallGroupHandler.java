package io.effi.rpc.processor;

import io.effi.rpc.annotation.rpc.CallGroup;
import io.effi.rpc.nativetools.ProxyConfig;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

/**
 * Handles the {@link CallGroup} annotation.
 */
public class CallGroupHandler extends AnnotationHandler<CallGroup> {

    protected CallGroupHandler(ProcessingEnvironment processingEnv) {
        super(CallGroup.class, processingEnv);
    }

    @Override
    protected void handle(Set<? extends Element> elements, RoundEnvironment roundEnv) {
        ProxyConfigResourceSection section = resourceSection(ProxyConfigResourceSection.class);
        for (Element element : elements) {
            if (element instanceof TypeElement typeElement) {
                String interfaceName = helper().qualifiedNameOf(typeElement);
                section.nativeConfig().addItem(new ProxyConfig.Item().addInterface(interfaceName));
            }
        }
    }
}
