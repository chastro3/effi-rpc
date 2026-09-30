package io.effi.rpc.compile;

import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.ReflectionUtil;
import io.effi.rpc.util.StringUtil;
import org.objectweb.asm.Type;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Provides utilities for compile-time processing tasks.
 * <p>
 * Facilitates annotation processing by offering helper methods for working with
 * elements, types, annotations, and bytecode generation using ASM.
 */
public class CompileTimeHelper {

    private static final Type OBJECT_TYPE = Type.getObjectType("java/lang/Object");

    private final ProcessingEnvironment processingEnv;

    private final Elements elements;

    private final Types types;

    public CompileTimeHelper(ProcessingEnvironment processingEnv) {
        this.processingEnv = AssertUtil.notNull(processingEnv, "processingEnv");
        this.elements = processingEnv.getElementUtils();
        this.types = processingEnv.getTypeUtils();
    }

    /**
     * Returns the annotation processing environment.
     */
    public ProcessingEnvironment processingEnv() {
        return processingEnv;
    }

    /**
     * Returns the qualified package name of the given type element.
     *
     * @param type target type element
     * @return qualified package name, or an empty string for the unnamed package
     */
    public String packageOf(TypeElement type) {
        return elements.getPackageOf(type).getQualifiedName().toString();
    }

    /**
     * Finds a generated class output resource.
     *
     * @param filePath output resource path
     * @return file object for the generated resource
     * @throws IOException if the resource cannot be located
     */
    public FileObject findOutputFile(String filePath) throws IOException {
        return processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, StringUtil.empty(), filePath);
    }

    /**
     * Creates a generated class output resource in the unnamed package.
     *
     * @param filePath output resource path
     * @return file object for the created resource
     * @throws IOException if the resource cannot be created
     */
    public FileObject createOutputFile(String filePath) throws IOException {
        return createOutputFile(StringUtil.empty(), filePath);
    }

    /**
     * Creates a generated class output resource.
     *
     * @param packageName target package name
     * @param filePath output resource path
     * @param originatingElements elements that caused the resource generation
     * @return file object for the created resource
     * @throws IOException if the resource cannot be created
     */
    public FileObject createOutputFile(String packageName, String filePath, Element... originatingElements) throws IOException {
        return processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, packageName, filePath, originatingElements);
    }

    /**
     * Converts a modifier set to an int bitmask compatible with {@link java.lang.reflect.Modifier}.
     *
     * @param mods source modifiers
     * @return reflection-compatible modifier bitmask
     */
    public int toReflectModifiers(Set<Modifier> mods) {
        int result = 0;
        for (Modifier m : mods) {
            switch (m) {
                case PUBLIC -> result |= java.lang.reflect.Modifier.PUBLIC;
                case PROTECTED -> result |= java.lang.reflect.Modifier.PROTECTED;
                case PRIVATE -> result |= java.lang.reflect.Modifier.PRIVATE;
                case STATIC -> result |= java.lang.reflect.Modifier.STATIC;
                case FINAL -> result |= java.lang.reflect.Modifier.FINAL;
                case ABSTRACT -> result |= java.lang.reflect.Modifier.ABSTRACT;
                case DEFAULT -> result |= 0x1000;
                default -> {
                }
            }
        }
        return result;
    }

    /**
     * Builds an erased method signature in {@code methodName(paramType1,paramType2,...)} form.
     *
     * @param method target executable element
     * @return erased method signature
     */
    public String buildSignature(ExecutableElement method) {
        StringBuilder sb = new StringBuilder();
        sb.append(method.getSimpleName()).append("(");
        List<? extends VariableElement> params = method.getParameters();
        int size = params.size();
        for (int i = 0; i < size; i++) {
            if (i > 0) sb.append(",");
            TypeMirror type = params.get(i).asType();
            sb.append(types.erasure(type).toString());
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * Returns the fully qualified class name using binary-name notation for nested types.
     *
     * @param typeElement target type element
     * @return qualified class name
     */
    public String qualifiedNameOf(TypeElement typeElement) {
        StringBuilder sb = new StringBuilder(64);
        Element current = typeElement;
        while (current.getKind().isClass() || current.getKind().isInterface()) {
            sb.insert(0, current.getSimpleName());
            current = current.getEnclosingElement();
            if (current.getKind().isClass() || current.getKind().isInterface()) {
                sb.insert(0, '$');
            }
        }
        PackageElement pkg = (PackageElement) current;
        if (!pkg.isUnnamed()) {
            sb.insert(0, pkg.getQualifiedName().toString() + ".");
        }
        return sb.toString();
    }

    /**
     * Extracts the fully qualified class name of a Class-valued annotation attribute.
     *
     * @param element annotated element
     * @param type annotation type
     * @param attributeName target attribute name
     * @return qualified class name, or {@code null} when the attribute is absent
     */
    public String extractClassName(Element element, Class<? extends Annotation> type, String attributeName) {
        AnnotationMirror annotationMirror = findAnnotationMirror(element, type);
        for (var entry : annotationMirror.getElementValues().entrySet()) {
            if (entry.getKey().getSimpleName().contentEquals(attributeName)) {
                TypeMirror typeMirror = (TypeMirror) entry.getValue().getValue();
                return qualifiedNameOf(asType(typeMirror));
            }
        }
        return null;
    }

    /**
     * Extracts a list of fully qualified class names from a Class[]-valued annotation attribute.
     *
     * @param element annotated element
     * @param type annotation type
     * @param attributeName target attribute name
     * @return qualified class names, or an empty list when the attribute is absent
     */
    @SuppressWarnings("unchecked")
    public List<String> extractClassNames(Element element, Class<? extends Annotation> type, String attributeName) {
        AnnotationMirror annotationMirror = findAnnotationMirror(element, type);
        for (var entry : annotationMirror.getElementValues().entrySet()) {
            if (entry.getKey().getSimpleName().contentEquals(attributeName)) {
                List<? extends AnnotationValue> values = (List<? extends AnnotationValue>)
                        entry.getValue().getValue();
                List<String> result = new ArrayList<>();
                for (AnnotationValue val : values) {
                    TypeMirror typeMirror = (TypeMirror) val.getValue();
                    result.add(qualifiedNameOf(asType(typeMirror)));
                }
                return result;
            }
        }
        return Collections.emptyList();
    }


    /**
     * Collects all implemented interface names recursively.
     *
     * @param typeElement target type element
     * @param filter predicate selecting interfaces to collect
     * @return qualified names of matching interfaces
     */
    public Set<String> findAllInterfaceNames(TypeElement typeElement, Predicate<TypeElement> filter) {
        Set<TypeElement> result = new LinkedHashSet<>();
        collectInterfaces(typeElement, result, filter);
        return result.stream()
                .map(this::qualifiedNameOf)
                .collect(Collectors.toSet());
    }

    /**
     * Returns the annotation mirror of the specified type on an element.
     *
     * @param element target element
     * @param type annotation type
     * @return matching annotation mirror, or {@code null} when the annotation is absent
     */
    public AnnotationMirror findAnnotationMirror(Element element, Class<? extends Annotation> type) {
        return findAnnotationMirror(element, type.getName());
    }

    /**
     * Returns the annotation mirror with the specified qualified name.
     *
     * @param element target element
     * @param annotationName qualified annotation name
     * @return matching annotation mirror, or {@code null} when the annotation is absent
     */
    public AnnotationMirror findAnnotationMirror(Element element, String annotationName) {
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            TypeElement type = asType(annotationMirror);
            if (qualifiedNameOf(type).contentEquals(annotationName)) {
                return annotationMirror;
            }
        }
        return null;
    }

    /**
     * Converts an annotation mirror to its type element.
     *
     * @param annotationMirror source annotation mirror
     * @return annotation type element
     */
    public TypeElement asType(AnnotationMirror annotationMirror) {
        return (TypeElement) types.asElement(annotationMirror.getAnnotationType());
    }

    /**
     * Converts a type mirror to its type element.
     *
     * @param typeMirror source type mirror
     * @return declared type element
     */
    public TypeElement asType(TypeMirror typeMirror) {
        return (TypeElement) types.asElement(typeMirror);
    }

    /**
     * Converts a type mirror to ASM's Type representation.
     *
     * @param mirror source type mirror
     * @return ASM type representation
     */
    public Type asAsmType(TypeMirror mirror) {
        switch (mirror.getKind()) {
            case BOOLEAN:
                return Type.BOOLEAN_TYPE;
            case BYTE:
                return Type.BYTE_TYPE;
            case SHORT:
                return Type.SHORT_TYPE;
            case INT:
                return Type.INT_TYPE;
            case LONG:
                return Type.LONG_TYPE;
            case CHAR:
                return Type.CHAR_TYPE;
            case FLOAT:
                return Type.FLOAT_TYPE;
            case DOUBLE:
                return Type.DOUBLE_TYPE;
            case VOID:
                return Type.VOID_TYPE;

            case ARRAY:
                ArrayType at = (ArrayType) mirror;
                Type elem = asAsmType(at.getComponentType());
                return Type.getType("[" + elem.getDescriptor());

            case DECLARED:
                DeclaredType dt = (DeclaredType) mirror;
                Element e = dt.asElement();
                if (e instanceof TypeElement typeElement) {
                    String qName = qualifiedNameOf(typeElement);
                    String internal = qName.replace('.', '/');
                    return Type.getObjectType(internal);
                }
                throw new IllegalStateException("Unknown DECLARED element: " + e);

            case TYPEVAR:
                TypeVariable tv = (TypeVariable) mirror;
                TypeMirror upper = tv.getUpperBound();
                if (upper != null && upper.getKind() != TypeKind.NONE) {
                    return asAsmType(upper);
                } else {
                    return OBJECT_TYPE;
                }

            case INTERSECTION:
                IntersectionType it = (IntersectionType) mirror;
                List<? extends TypeMirror> bounds = it.getBounds();
                if (!bounds.isEmpty()) {
                    return asAsmType(bounds.get(0));
                } else {
                    return OBJECT_TYPE;
                }

            case WILDCARD:
                WildcardType wt = (WildcardType) mirror;
                TypeMirror extendsBound = wt.getExtendsBound();
                if (extendsBound != null) {
                    return asAsmType(extendsBound);
                } else {
                    return OBJECT_TYPE;
                }

            case NULL:
                throw new IllegalArgumentException("NULL kind is not supported for ASM generation");

            case NONE:
                throw new IllegalArgumentException("NONE kind is not supported for ASM generation");

            case ERROR:
                throw new IllegalStateException("Encountered ERROR type: " + mirror);
            case UNION:
                throw new IllegalArgumentException("UNION kind is not supported for ASM generation");

            default:
                throw new IllegalArgumentException("Unsupported TypeKind: " + mirror.getKind());
        }
    }

    /**
     * Checks if the method is defined in java.lang.Object.
     *
     * @param element target executable element
     * @return {@code true} when the method belongs to {@link Object}
     */
    public boolean isObjectMethod(ExecutableElement element) {
        String methodName = element.getSimpleName().toString();
        return ReflectionUtil.isObjectMethod(methodName, () -> buildSignature(element));
    }

    private void collectInterfaces(TypeElement typeElement, Set<TypeElement> collectedList, Predicate<TypeElement> filter) {
        if (typeElement == null || qualifiedNameOf(typeElement).contentEquals("java.lang.Object")) {
            return;
        }
        for (TypeMirror interfaceMirror : typeElement.getInterfaces()) {
            if (interfaceMirror.getKind() != TypeKind.DECLARED) {
                continue;
            }
            Element element = ((DeclaredType) interfaceMirror).asElement();
            if (element.getKind() == ElementKind.INTERFACE && element instanceof TypeElement interfaceElement) {
                if (filter.test(interfaceElement)) {
                    collectedList.add(interfaceElement);
                }
                collectInterfaces(interfaceElement, collectedList, filter);
            }
        }
        TypeMirror superclass = typeElement.getSuperclass();
        if (superclass.getKind() == TypeKind.DECLARED) {
            TypeElement superClassElement = asType(superclass);
            collectInterfaces(superClassElement, collectedList, filter);
        }
    }
}
