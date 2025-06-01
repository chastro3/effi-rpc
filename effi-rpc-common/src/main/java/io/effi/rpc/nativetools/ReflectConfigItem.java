package io.effi.rpc.nativetools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a reflection configuration item in reflect-config.
 */
public class ReflectConfigItem implements Item {

    /**
     * Optional. Only register when this class is reachable.
     */
    private ConditionItem condition;

    /**
     * Required. Type descriptor of the class ("type" property).
     */
    private String type;

    /**
     * Optional. Deprecated alias for "type".
     */
    //@Deprecated
    private String name;

    /**
     * Optional. Methods to register for reflection.
     */
    private final List<MethodConfigItem> methods = new ArrayList<>();

    /**
     * Optional. Methods to register for lookup only.
     */
    private final List<MethodConfigItem> queriedMethods = new ArrayList<>();

    /**
     * Optional. Field names to register for reflection.
     */
    private List<String> fields = new ArrayList<>();

    /**
     * Optional. Register declared inner classes.
     */
    private Boolean allDeclaredClasses;

    /**
     * Optional. Register declared methods.
     */
    private Boolean allDeclaredMethods;

    /**
     * Optional. Register declared fields.
     */
    private Boolean allDeclaredFields;

    /**
     * Optional. Register declared constructors.
     */
    private Boolean allDeclaredConstructors;

    /**
     * Optional. Register public inner classes.
     */
    private Boolean allPublicClasses;

    /**
     * Optional. Register public methods.
     */
    private Boolean allPublicMethods;

    /**
     * Optional. Register public fields.
     */
    private Boolean allPublicFields;

    /**
     * Optional. Register public constructors.
     */
    private Boolean allPublicConstructors;

    /**
     * Optional. Register record components (if record).
     */
    private Boolean allRecordComponents;

    /**
     * Optional. Register permitted subclasses (if sealed).
     */
    private Boolean allPermittedSubclasses;

    /**
     * Optional. Register nest members.
     */
    private Boolean allNestMembers;

    /**
     * Optional. Register class signers.
     */
    private Boolean allSigners;

    /**
     * Optional. Query declared methods only.
     */
    private Boolean queryAllDeclaredMethods;

    /**
     * Optional. Query declared constructors only.
     */
    private Boolean queryAllDeclaredConstructors;

    /**
     * Optional. Query public methods only.
     */
    private Boolean queryAllPublicMethods;

    /**
     * Optional. Query public constructors only.
     */
    private Boolean queryAllPublicConstructors;

    /**
     * Optional. Allow Unsafe.allocateInstance().
     */
    private Boolean unsafeAllocated;

    public ReflectConfigItem condition(ConditionItem condition) {
        this.condition = condition;
        return this;
    }

    public ReflectConfigItem type(String type) {
        boolean gt22 = NativeUtil.isGraalVMVersionAtLeast("22");
        if (gt22) {
            this.type = type;
        } else {
            name(type);
        }
        return this;
    }

    //@Deprecated
    public ReflectConfigItem name(String name) {
        this.name = name;
        return this;
    }

    public ReflectConfigItem method(String name, List<String> parameterTypes) {
        methods.add(new MethodConfigItem(name, parameterTypes));
        return this;
    }

    public ReflectConfigItem queriedMethod(String name, List<String> parameterTypes) {
        queriedMethods.add(new MethodConfigItem(name, parameterTypes));
        return this;
    }

    public ReflectConfigItem fields(List<String> fields) {
        this.fields = fields;
        return this;
    }

    public ReflectConfigItem allDeclaredClasses(Boolean v) {
        this.allDeclaredClasses = v;
        return this;
    }

    public ReflectConfigItem allDeclaredMethods(Boolean v) {
        this.allDeclaredMethods = v;
        return this;
    }

    public ReflectConfigItem allDeclaredFields(Boolean v) {
        this.allDeclaredFields = v;
        return this;
    }

    public ReflectConfigItem allDeclaredConstructors(Boolean v) {
        this.allDeclaredConstructors = v;
        return this;
    }

    public ReflectConfigItem allPublicClasses(Boolean v) {
        this.allPublicClasses = v;
        return this;
    }

    public ReflectConfigItem allPublicMethods(Boolean v) {
        this.allPublicMethods = v;
        return this;
    }

    public ReflectConfigItem allPublicFields(Boolean v) {
        this.allPublicFields = v;
        return this;
    }

    public ReflectConfigItem allPublicConstructors(Boolean v) {
        this.allPublicConstructors = v;
        return this;
    }

    public ReflectConfigItem allRecordComponents(Boolean v) {
        this.allRecordComponents = v;
        return this;
    }

    public ReflectConfigItem allPermittedSubclasses(Boolean v) {
        this.allPermittedSubclasses = v;
        return this;
    }

    public ReflectConfigItem allNestMembers(Boolean v) {
        this.allNestMembers = v;
        return this;
    }

    public ReflectConfigItem allSigners(Boolean v) {
        this.allSigners = v;
        return this;
    }

    public ReflectConfigItem queryAllDeclaredMethods(Boolean v) {
        this.queryAllDeclaredMethods = v;
        return this;
    }

    public ReflectConfigItem queryAllDeclaredConstructors(Boolean v) {
        this.queryAllDeclaredConstructors = v;
        return this;
    }

    public ReflectConfigItem queryAllPublicMethods(Boolean v) {
        this.queryAllPublicMethods = v;
        return this;
    }

    public ReflectConfigItem queryAllPublicConstructors(Boolean v) {
        this.queryAllPublicConstructors = v;
        return this;
    }

    public ReflectConfigItem unsafeAllocated(Boolean v) {
        this.unsafeAllocated = v;
        return this;
    }

    @Override
    public Map<String, Object> toMap() {
        return MapBuilder.create(4)
                .put("condition", condition)
                .put("type", type)
                .put("name", name)
                .put("methods", methods)
                .put("queriedMethods", queriedMethods)
                .put("fields", fields)
                .put("allDeclaredClasses", allDeclaredClasses)
                .put("allDeclaredMethods", allDeclaredMethods)
                .put("allDeclaredFields", allDeclaredFields)
                .put("allDeclaredConstructors", allDeclaredConstructors)
                .put("allPublicClasses", allPublicClasses)
                .put("allPublicMethods", allPublicMethods)
                .put("allPublicFields", allPublicFields)
                .put("allPublicConstructors", allPublicConstructors)
                .put("allRecordComponents", allRecordComponents)
                .put("allPermittedSubclasses", allPermittedSubclasses)
                .put("allNestMembers", allNestMembers)
                .put("allSigners", allSigners)
                .put("queryAllDeclaredMethods", queryAllDeclaredMethods)
                .put("queryAllDeclaredConstructors", queryAllDeclaredConstructors)
                .put("queryAllPublicMethods", queryAllPublicMethods)
                .put("queryAllPublicConstructors", queryAllPublicConstructors)
                .put("unsafeAllocated", unsafeAllocated)
                .build();
    }

    static class MethodConfigItem implements Item {

        private final String name;
        private final List<String> parameterTypes;

        MethodConfigItem(String name, List<String> parameterTypes) {
            this.name = name;
            this.parameterTypes = parameterTypes;
        }


        @Override
        public Map<String, Object> toMap() {
            return MapBuilder.create(2)
                    .put("name", name)
                    .put("parameterTypes", parameterTypes)
                    .build();
        }
    }
}



