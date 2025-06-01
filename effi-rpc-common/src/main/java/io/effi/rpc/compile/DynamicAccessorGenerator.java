package io.effi.rpc.compile;

import io.effi.rpc.util.CollectionUtil;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.objectweb.asm.Opcodes.AALOAD;
import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.F_SAME;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_8;

/**
 * Generates {@link DynamicAccessor} at compile-time and runtime.
 * Collects methods and constructs dynamic accessor classes.
 */
public class DynamicAccessorGenerator {

    private static final String METHOD_NAMES = "METHOD_NAMES";

    private static final String PARAMETER_TYPES = "PARAMETER_TYPES";

    /**
     * Generates {@link GeneratedInfo} from a runtime class.
     */
    public static GeneratedInfo fromClass(Class<?> type) {
        String pkg = type.getPackage().getName();
        String name = type.getSimpleName();
        String qualifiedName = type.getName();
        Method[] methods = type.getMethods();
        List<MethodInfo> methodInfos = new ArrayList<>(methods.length);
        for (Method method : methods) {
            if (!method.isBridge() && !method.isSynthetic() && method.getDeclaringClass() != Object.class) {
                methodInfos.add(MethodInfo.fromMethod(method));
            }
        }
        MethodInfo[] infos = methodInfos.toArray(new MethodInfo[0]);
        return generate(new ClassInfo(pkg, name, qualifiedName, infos, type.isInterface()));
    }

    /**
     * Generates {@link GeneratedInfo} from a compile-time type element.
     */
    public static GeneratedInfo fromTypeElement(TypeElement type, CompileTimeHelper helper) {
        String pkg = helper.getPackage(type);
        String qualifiedName = helper.getQualifiedClassName(type);
        String name = type.getSimpleName().toString();
        List<? extends Element> members = helper.processingEnv().getElementUtils().getAllMembers(type);
        List<MethodInfo> methods = new ArrayList<>(members.size());
        for (Element e : members) {
            if (e.getKind() != ElementKind.METHOD) continue;
            ExecutableElement m = (ExecutableElement) e;
            if (!m.getModifiers().contains(javax.lang.model.element.Modifier.PUBLIC)) continue;
            if (helper.isObjectMethod(m)) continue;
            methods.add(MethodInfo.fromExecutableElement(m, helper));
        }
        MethodInfo[] infos = methods.toArray(new MethodInfo[0]);
        return generate(new ClassInfo(pkg, name, qualifiedName, infos, type.getKind() == ElementKind.INTERFACE));
    }

    private static GeneratedInfo generate(ClassInfo info) {
        String pkgInternal = info.pkg().replace('.', '/') + '/';
        String internal = pkgInternal + info.name() + DynamicAccessor.SUFFIX;
        String targetInternal = info.qualifiedName().replace('.', '/');

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(V1_8, ACC_PUBLIC | ACC_SUPER, internal, null, DynamicAccessor.INTERNAL_NAME, null);
        boolean hasMethods = CollectionUtil.isNotEmpty(info.methods());
        if (hasMethods) emitStaticInitializer(cw, internal, info);
        emitConstructor(cw, internal, targetInternal, hasMethods);
        if (hasMethods) emitInvoke(cw, targetInternal, info);

        cw.visitEnd();
        return new GeneratedInfo(info.pkg(), info.name() + DynamicAccessor.SUFFIX, cw.toByteArray());
    }

    private static void emitStaticInitializer(ClassWriter cw, String owner, ClassInfo info) {
        cw.visitField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, METHOD_NAMES, "[Ljava/lang/String;", null, null).visitEnd();
        cw.visitField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, PARAMETER_TYPES, "[[Ljava/lang/Class;", null, null).visitEnd();

        MethodVisitor mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
        mv.visitCode();
        emitStringArray(mv, owner, info.methods(), MethodInfo::name);
        emitClassArray(mv, owner, info.methods(), MethodInfo::parameterTypes);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private static void emitStringArray(MethodVisitor mv, String owner, MethodInfo[] methods, Function<MethodInfo, String> f) {
        int n = methods.length;
        mv.visitIntInsn(BIPUSH, n);
        mv.visitTypeInsn(ANEWARRAY, "java/lang/String");
        for (int i = 0; i < n; i++) {
            mv.visitInsn(DUP);
            mv.visitIntInsn(BIPUSH, i);
            mv.visitLdcInsn(f.apply(methods[i]));
            mv.visitInsn(AASTORE);
        }
        mv.visitFieldInsn(PUTSTATIC, owner, METHOD_NAMES, "[Ljava/lang/String;");
    }

    private static void emitClassArray(MethodVisitor mv, String owner, MethodInfo[] methods, Function<MethodInfo, Type[]> f) {
        int n = methods.length;
        mv.visitIntInsn(BIPUSH, n);
        mv.visitTypeInsn(ANEWARRAY, "[Ljava/lang/Class;");

        for (int i = 0; i < n; i++) {
            Type[] pts = f.apply(methods[i]);

            mv.visitInsn(DUP);
            mv.visitIntInsn(BIPUSH, i);

            if (pts.length == 0) {
                mv.visitInsn(ACONST_NULL);
            } else {
                mv.visitIntInsn(BIPUSH, pts.length);
                mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
                for (int j = 0; j < pts.length; j++) {
                    mv.visitInsn(DUP);
                    mv.visitIntInsn(BIPUSH, j);
                    Type t = pts[j];
                    if (t.getSort() <= Type.DOUBLE) {
                        mv.visitFieldInsn(GETSTATIC, PrimitiveInfo.of(t.getSort()).wrapperInternal, "TYPE", "Ljava/lang/Class;");
                    } else {
                        mv.visitLdcInsn(t);
                    }
                    mv.visitInsn(AASTORE);
                }
            }
            mv.visitInsn(AASTORE);
        }
        mv.visitFieldInsn(PUTSTATIC, owner, PARAMETER_TYPES, "[[Ljava/lang/Class;");
    }

    private static void emitConstructor(ClassWriter cw, String owner, String targetInternal, boolean hasMethods) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitLdcInsn(Type.getObjectType(targetInternal));

        if (hasMethods) {
            mv.visitFieldInsn(GETSTATIC, owner, METHOD_NAMES, "[Ljava/lang/String;");
            mv.visitFieldInsn(GETSTATIC, owner, PARAMETER_TYPES, "[[Ljava/lang/Class;");
        } else {
            mv.visitInsn(ACONST_NULL);
            mv.visitInsn(ACONST_NULL);
        }

        mv.visitMethodInsn(INVOKESPECIAL, DynamicAccessor.INTERNAL_NAME,
                "<init>", "(Ljava/lang/Class;[Ljava/lang/String;[[Ljava/lang/Class;)V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(4, 1);
        mv.visitEnd();
    }

    private static void emitInvoke(ClassWriter cw, String targetInternal, ClassInfo info) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "invoke", "(Ljava/lang/Object;I[Ljava/lang/Object;)Ljava/lang/Object;", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 1);
        mv.visitTypeInsn(CHECKCAST, targetInternal);
        mv.visitVarInsn(ASTORE, 4);
        emitMethodSwitch(mv, targetInternal, info);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private static void emitMethodSwitch(MethodVisitor mv, String targetInternal, ClassInfo info) {
        MethodInfo[] methods = info.methods();
        int n = methods.length;
        mv.visitVarInsn(ILOAD, 2);
        Label[] labels = new Label[n];
        for (int i = 0; i < n; i++) labels[i] = new Label();
        Label dfl = new Label();
        mv.visitTableSwitchInsn(0, n - 1, dfl, labels);

        for (int i = 0; i < n; i++) {
            MethodInfo mth = methods[i];
            mv.visitLabel(labels[i]);
            if (i == 0) {
                mv.visitFrame(Opcodes.F_APPEND, 1, new Object[]{targetInternal}, 0, null);
            } else {
                mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            }
            if (!Modifier.isStatic(mth.modifiers())) {
                mv.visitVarInsn(ALOAD, 4);
            }

            Type[] pts = mth.parameterTypes();
            for (int j = 0; j < pts.length; j++) {
                mv.visitVarInsn(ALOAD, 3);
                mv.visitLdcInsn(j);
                mv.visitInsn(AALOAD);
                emitParamUnboxing(mv, pts[j]);
            }
            int opcode = Modifier.isStatic(mth.modifiers()) ? INVOKESTATIC
                    : info.isInterface() ? INVOKEINTERFACE
                    : INVOKEVIRTUAL;
            mv.visitMethodInsn(opcode, targetInternal, mth.name(), mth.descriptor(), info.isInterface());
            emitReturnBoxing(mv, mth.returnType());
        }
        mv.visitLabel(dfl);
        mv.visitFrame(F_SAME, 0, null, 0, null);
        mv.visitTypeInsn(NEW, "java/lang/IllegalArgumentException");
        mv.visitInsn(DUP);
        mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
        mv.visitInsn(DUP);
        mv.visitLdcInsn("Method not found: ");
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
        mv.visitVarInsn(ILOAD, 2);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>", "(Ljava/lang/String;)V", false);
        mv.visitInsn(ATHROW);
    }

    private static void emitParamUnboxing(MethodVisitor mv, Type t) {
        int sort = t.getSort();
        PrimitiveInfo info = PrimitiveInfo.of(sort);
        if (info != null) {
            mv.visitTypeInsn(CHECKCAST, info.wrapperInternal);
            mv.visitMethodInsn(INVOKEVIRTUAL, info.wrapperInternal, info.unboxMethodName, info.unboxDesc, false);
        } else {
            mv.visitTypeInsn(CHECKCAST, t.getInternalName());
        }
    }

    private static void emitReturnBoxing(MethodVisitor mv, Type rt) {
        int sort = rt.getSort();
        if (sort == Type.VOID) {
            mv.visitInsn(ACONST_NULL);
        } else {
            PrimitiveInfo info = PrimitiveInfo.of(sort);
            if (info != null) {
                mv.visitMethodInsn(INVOKESTATIC, info.wrapperInternal, "valueOf", info.boxDesc, false);
            }
        }
        mv.visitInsn(ARETURN);
    }

    record ClassInfo(String pkg, String name, String qualifiedName, MethodInfo[] methods, boolean isInterface) {
    }

    record MethodInfo(String name, Type[] parameterTypes, Type returnType, String descriptor, int modifiers) {

        public static MethodInfo fromExecutableElement(ExecutableElement element, CompileTimeHelper helper) {
            String name = element.getSimpleName().toString();
            List<? extends VariableElement> params = element.getParameters();
            int size = params.size();
            Type[] parameterTypes = new Type[size];
            for (int i = 0; i < size; i++) {
                parameterTypes[i] = helper.asAsmType(params.get(i).asType());
            }
            Type returnType = helper.asAsmType(element.getReturnType());
            int modifiers = helper.toReflectModifiers(element.getModifiers());
            String descriptor = Type.getMethodDescriptor(returnType, parameterTypes);
            return new MethodInfo(name, parameterTypes, returnType, descriptor, modifiers);
        }

        public static MethodInfo fromMethod(Method method) {
            String name = method.getName();
            Class<?>[] paramTypes = method.getParameterTypes();
            int size = paramTypes.length;
            Type[] parameterTypes = new Type[size];
            for (int i = 0; i < size; i++) {
                parameterTypes[i] = Type.getType(paramTypes[i]);
            }
            Type returnType = Type.getType(method.getReturnType());
            String descriptor = Type.getMethodDescriptor(method);
            return new MethodInfo(name, parameterTypes, returnType, descriptor, method.getModifiers());
        }
    }

    enum PrimitiveInfo {
        BOOLEAN(Type.BOOLEAN, "java/lang/Boolean", "booleanValue", "()Z", "(Z)Ljava/lang/Boolean;"),
        BYTE(Type.BYTE, "java/lang/Byte", "byteValue", "()B", "(B)Ljava/lang/Byte;"),
        CHAR(Type.CHAR, "java/lang/Character", "charValue", "()C", "(C)Ljava/lang/Character;"),
        SHORT(Type.SHORT, "java/lang/Short", "shortValue", "()S", "(S)Ljava/lang/Short;"),
        INT(Type.INT, "java/lang/Integer", "intValue", "()I", "(I)Ljava/lang/Integer;"),
        FLOAT(Type.FLOAT, "java/lang/Float", "floatValue", "()F", "(F)Ljava/lang/Float;"),
        LONG(Type.LONG, "java/lang/Long", "longValue", "()J", "(J)Ljava/lang/Long;"),
        DOUBLE(Type.DOUBLE, "java/lang/Double", "doubleValue", "()D", "(D)Ljava/lang/Double;");

        public final int sort;

        public final String wrapperInternal;

        public final String unboxMethodName;

        public final String unboxDesc;

        public final String boxDesc;

        PrimitiveInfo(int sort, String wrapperInternal, String unboxMethodName, String unboxDesc, String boxDesc) {
            this.sort = sort;
            this.wrapperInternal = wrapperInternal;
            this.unboxMethodName = unboxMethodName;
            this.unboxDesc = unboxDesc;
            this.boxDesc = boxDesc;
        }

        private static final Map<Integer, PrimitiveInfo> SORT_LOOKUP = new HashMap<>();

        static {
            for (PrimitiveInfo info : values()) {
                SORT_LOOKUP.put(info.sort, info);
            }
        }

        public static PrimitiveInfo of(int sort) {
            return SORT_LOOKUP.get(sort);
        }
    }
}