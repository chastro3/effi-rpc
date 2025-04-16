package io.effi.rpc.nativetools;

import java.util.*;

public class ReflectConfigItem implements Item {

    private String conditionClass;

    private String className;

    private final List<MethodConfigItem> methods = new ArrayList<>();

    private List<String> fields;

    private Boolean allDeclaredConstructors;

    private Boolean allPublicConstructors;

    private Boolean allDeclaredMethods;

    private Boolean allPublicMethods;

    private Boolean allDeclaredFields;

    private Boolean allPublicFields;

    private Boolean unsafeAllocated;

    private Boolean queryAllDeclaredMethods;

    private Boolean queryAllPublicMethods;

    public ReflectConfigItem className(String className) {
        this.className = className;
        return this;
    }

    public ReflectConfigItem conditionClass(String conditionClass) {
        this.conditionClass = conditionClass;
        return this;
    }

    public ReflectConfigItem fields(List<String> fields) {
        this.fields = fields;
        return this;
    }

    public ReflectConfigItem allDeclaredConstructors(Boolean allDeclaredConstructors) {
        this.allDeclaredConstructors = allDeclaredConstructors;
        return this;
    }

    public ReflectConfigItem allPublicConstructors(Boolean allPublicConstructors) {
        this.allPublicConstructors = allPublicConstructors;
        return this;
    }

    public ReflectConfigItem allDeclaredMethods(Boolean allDeclaredMethods) {
        this.allDeclaredMethods = allDeclaredMethods;
        return this;
    }

    public ReflectConfigItem allPublicMethods(Boolean allPublicMethods) {
        this.allPublicMethods = allPublicMethods;
        return this;
    }

    public ReflectConfigItem allDeclaredFields(Boolean allDeclaredFields) {
        this.allDeclaredFields = allDeclaredFields;
        return this;
    }

    public ReflectConfigItem allPublicFields(Boolean allPublicFields) {
        this.allPublicFields = allPublicFields;
        return this;
    }

    public ReflectConfigItem unsafeAllocated(Boolean unsafeAllocated) {
        this.unsafeAllocated = unsafeAllocated;
        return this;
    }

    public ReflectConfigItem queryAllDeclaredMethods(Boolean queryAllDeclaredMethods) {
        this.queryAllDeclaredMethods = queryAllDeclaredMethods;
        return this;
    }

    public ReflectConfigItem queryAllPublicMethods(Boolean queryAllPublicMethods) {
        this.queryAllPublicMethods = queryAllPublicMethods;
        return this;
    }

    public ReflectConfigItem addMethod(String name, List<String> parameterTypes) {
        methods.add(new MethodConfigItem(name, parameterTypes));
        return this;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>(10);
        if (conditionClass != null)
            map.put("condition", Map.of("typeReachable", conditionClass));
        if (className != null)
            map.put("name", className);
        if (!methods.isEmpty())
            map.put("methods", methods());
        if (fields != null)
            map.put("fields", fields);
        Item.fillBoolean(map, "allDeclaredConstructors", allDeclaredConstructors);
        Item.fillBoolean(map, "allPublicConstructors", allPublicConstructors);
        Item.fillBoolean(map, "allDeclaredMethods", allDeclaredMethods);
        Item.fillBoolean(map, "allPublicMethods", allPublicMethods);
        Item.fillBoolean(map, "allDeclaredFields", allDeclaredFields);
        Item.fillBoolean(map, "allPublicFields", allPublicFields);
        Item.fillBoolean(map, "unsafeAllocated", unsafeAllocated);
        Item.fillBoolean(map, "queryAllDeclaredMethods", queryAllDeclaredMethods);
        Item.fillBoolean(map, "queryAllPublicMethods", queryAllPublicMethods);
        return map;
    }

    private List<Map<String, Object>> methods() {
        ArrayList<Map<String, Object>> result = new ArrayList<>();
        if (!methods.isEmpty()) {
            for (MethodConfigItem method : methods) {
                result.add(method.toMap());
            }
        }
        return result;
    }

    public static class MethodConfigItem implements Item {

        private final String name;

        private final List<String> parameterTypes;

        public MethodConfigItem(String name, List<String> parameterTypes) {
            this.name = name;
            this.parameterTypes = parameterTypes;
        }

        @Override
        public Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>(2);
            map.put("name", name);
            map.put("parameterTypes", parameterTypes == null ? Collections.emptyList() : parameterTypes);
            return map;
        }

    }
}
