package io.effi.rpc.common.extension.spi;

import io.effi.rpc.common.constant.Constant;
import io.effi.rpc.common.util.FileUtil;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Generates standard Service Provider Interface (SPI) files.
 * <p>
 * This processor scans the project's annotations to identify all classes marked as extension points and generates corresponding
 * SPI files for each extension point. The generated files will be placed in the META-INF/services directory, following the
 * Java SPI specification, facilitating dynamic loading and extension of services.
 * </p>
 *
 * @see Extensible
 * @see Extension
 */
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@SupportedAnnotationTypes({Constant.EXTENSION_NAME})
public class ExtensionAnnotationProcessor extends AbstractProcessor {

    private Messager messager;

    private Map<String, File> fileMap;
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        fileMap = new HashMap<>();
        messager = processingEnv.getMessager();
    }

    /**
     * Processes the {@link Extension} annotation and generates SPI files that map interface names to
     * implementation classes.
     *
     * @param set              the set of elements to be processed
     * @param roundEnvironment the environment for the current round of processing
     * @return true if the annotations are processed successfully
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> extensionElements = roundEnvironment.getElementsAnnotatedWith(Extension.class);
        for (Element element : extensionElements) {
            if (element instanceof TypeElement typeElement) {
                // Get @Extension
                AnnotationMirror extensionMirror = typeElement.getAnnotationMirrors().stream()
                        .filter(annotationMirror -> annotationMirror.getAnnotationType().toString().equals(Constant.EXTENSION_NAME))
                        .toList()
                        .getFirst();
                // Get class's all interfaces.
                List<CharSequence> allInterfaces = getAllInterfaces(typeElement);
                // Get interfaces from @Extension#interfaces()
                List<CharSequence> interfaces = getInterfaces(extensionMirror);
                // Needs to be added to the SPI file.
                List<CharSequence> addedInterfaces = (interfaces == null)
                        ? allInterfaces
                        : allInterfaces.stream().filter(interfaces::contains).toList();
                for (CharSequence interfaceName : addedInterfaces) {
                    String path = Constant.SPI_FIX_PATH + interfaceName;
                    CharSequence content = getFullClassName(typeElement);
                    writeServiceFile(path, content);
                }
            }
        }
        return true;
    }

    private static CharSequence getFullClassName(TypeElement typeElement) {
        if (typeElement.getModifiers().contains(Modifier.STATIC)
                && typeElement.getEnclosingElement().getKind().equals(ElementKind.CLASS)) {
            String innerClassName = typeElement.getSimpleName().toString();
            return ((TypeElement) typeElement.getEnclosingElement()).getQualifiedName() + "$" + innerClassName;
        } else if (typeElement.getKind().equals(ElementKind.CLASS)) {
            return typeElement.getQualifiedName();
        }
        throw new IllegalArgumentException("Unsupported type element: " + typeElement);
    }

    /**
     * Collects all interfaces implemented by the specified {@link TypeElement}, including inherited interfaces.
     */
    private List<CharSequence> getAllInterfaces(TypeElement typeElement) {
        Set<TypeElement> allInterfaces = new HashSet<>();
        collectInterfacesRecursively(typeElement, allInterfaces);
        return allInterfaces.stream()
                .filter(item -> item.getAnnotation(Extensible.class) != null)
                .map(item -> (CharSequence) item.getQualifiedName())
                .toList();
    }

    /**
     * Recursively collects interfaces for the specified class and its parent classes.
     */
    private void collectInterfacesRecursively(TypeElement typeElement, Set<TypeElement> collectedInterfaces) {
        if (typeElement == null) {
            return;
        }
        for (TypeMirror interfaceMirror : typeElement.getInterfaces()) {
            if (interfaceMirror instanceof DeclaredType declaredType
                    && declaredType.asElement() instanceof TypeElement interfaceElement) {
                collectedInterfaces.add(interfaceElement);
                // Recursively gets the parent interface of the interface
                collectInterfacesRecursively(interfaceElement, collectedInterfaces);
            }
        }
        // Recursively get the interface of the parent class
        if (typeElement.getSuperclass() != null) {
            TypeElement superClassElement = getTypeElement(typeElement.getSuperclass());
            collectInterfacesRecursively(superClassElement, collectedInterfaces);
        }
    }

    /**
     * Retrieves the interfaces specified in the {@link Extension} annotation.
     */
    @SuppressWarnings("unchecked")
    private List<CharSequence> getInterfaces(AnnotationMirror annotationMirror) {
        var elementValues = annotationMirror.getElementValues();
        for (ExecutableElement key : elementValues.keySet()) {
            if (key.toString().equals("interfaces()")) {
                List<AnnotationValue> values = (List<AnnotationValue>) elementValues.get(key).getValue();
                return values.stream()
                        .map(annotationValue -> (TypeMirror) annotationValue.getValue())
                        .map(typeMirror -> (CharSequence) getTypeElement(typeMirror).getQualifiedName())
                        .toList();
            }
        }
        return null;
    }

    private TypeElement getTypeElement(TypeMirror typeMirror) {
        return (TypeElement) processingEnv.getTypeUtils().asElement(typeMirror);
    }

    /**
     * Writes the SPI file for the specified interface.
     */
    private void writeServiceFile(String path, CharSequence content) {
        File file = fileMap.computeIfAbsent(path, this::creatrFile);
        if (file != null) FileUtil.writeLineFile(content, file);
    }

    /**
     * Creates a file for the SPI file output.
     */
    private File creatrFile(String path) {
        try {
            FileObject fileObject = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", path);
            return new File(fileObject.toUri());
        } catch (IOException e) {
            messager.printError(e.getMessage());
        }
        return null;
    }
}

