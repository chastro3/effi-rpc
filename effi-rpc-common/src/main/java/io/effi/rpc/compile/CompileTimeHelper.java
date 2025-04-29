package io.effi.rpc.compile;

import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.ReflectionUtil;
import org.objectweb.asm.Type;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Provides utilities for compile-time processing tasks.
 * Handles types, elements, annotations, and bytecode operations.
 */
public class CompileTimeHelper {

    private final ProcessingEnvironment processingEnv;

    private final Elements elements;

    private final Types types;

    public CompileTimeHelper(ProcessingEnvironment processingEnv) {
        this.processingEnv = AssertUtil.notNull(processingEnv, "processingEnv");
        this.elements = processingEnv.getElementUtils();
        this.types = processingEnv.getTypeUtils();
    }

    public ProcessingEnvironment processingEnv() {
        return processingEnv;
    }

    /**
     * Gets the package name of the given type element.
     */
    public String getPackage(TypeElement type) {
        return elements.getPackageOf(type).getQualifiedName().toString();
    }

    /**
     * Converts a set of modifiers into int bitmask compatible with {@link java.lang.reflect.Modifier}.
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
     * Builds method signature string: methodName(paramType1,paramType2,...).
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
     * Gets the fully qualified class name including inner class $ notation.
     */
    public  String getQualifiedClassName(TypeElement typeElement) {
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
     * Collects all implemented interface names recursively.
     */
    public Set<Name> getAllInterfaceNames(TypeElement typeElement, Predicate<TypeElement> filter) {
        Set<Name> result = new LinkedHashSet<>();
        collectInterfacesRecursively(typeElement, result, filter);
        return result;
    }

    /**
     * Returns the annotation mirror of the specified type on an element.
     */
    public AnnotationMirror getAnnotationMirror(Element element, Class<? extends Annotation> type) {
        return getAnnotationMirror(element, type.getName());
    }

    /**
     * Returns the type mirror with the specified qualified name.
     */
    public AnnotationMirror getAnnotationMirror(Element element, String annotationName) {
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            TypeElement type = asType(annotationMirror);
            if (type.getQualifiedName().contentEquals(annotationName)) {
                return annotationMirror;
            }
        }
        return null;
    }

    /**
     * Converts an annotation mirror to its type element.
     */
    public TypeElement asType(AnnotationMirror annotationMirror) {
        return (TypeElement) types.asElement(annotationMirror.getAnnotationType());
    }

    /**
     * Converts an annotation mirror to its type element.
     */
    public TypeElement asType(TypeMirror typeMirror) {
        return (TypeElement) types.asElement(typeMirror);
    }

    /**
     * Converts a type mirror to ASM's Type representation.
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
                String qName = ((TypeElement) types.asElement(dt)).getQualifiedName().toString();
                String internal = qName.replace('.', '/');
                return Type.getObjectType(internal);
            default:
                throw new IllegalArgumentException("Unsupported kind: " + mirror.getKind());
        }
    }

    /**
     * Returns the ASM method descriptor for a method.
     */
    public String getMethodDescriptor(ExecutableElement element) {
        Type returnType = asAsmType(element.getReturnType());
        Type[] args = element.getParameters().stream()
                .map(p -> asAsmType(p.asType()))
                .toArray(Type[]::new);
        return Type.getMethodDescriptor(returnType, args);
    }

    /**
     * Checks if the method is defined in java.lang.Object.
     */
    public boolean isObjectMethod(ExecutableElement element) {
        String methodName = element.getSimpleName().toString();
        return ReflectionUtil.isObjectMethod(methodName, () -> buildSignature(element));
    }

    private void collectInterfacesRecursively(TypeElement typeElement, Set<Name> collectedNames, Predicate<TypeElement> filter) {
        if (typeElement == null || typeElement.getQualifiedName().contentEquals("java.lang.Object")) {
            return;
        }
        for (TypeMirror interfaceMirror : typeElement.getInterfaces()) {
            if (interfaceMirror.getKind() != TypeKind.DECLARED) {
                continue;
            }
            Element element = ((DeclaredType) interfaceMirror).asElement();
            if (element.getKind() == ElementKind.INTERFACE && element instanceof TypeElement interfaceElement) {
                if (filter.test(interfaceElement) && collectedNames.add(interfaceElement.getQualifiedName())) {
                    collectInterfacesRecursively(interfaceElement, collectedNames, filter);
                }
            }
        }
        TypeMirror superclass = typeElement.getSuperclass();
        if (superclass.getKind() == TypeKind.DECLARED) {
            TypeElement superClassElement = asType(superclass);
            collectInterfacesRecursively(superClassElement, collectedNames, filter);
        }
    }

}
