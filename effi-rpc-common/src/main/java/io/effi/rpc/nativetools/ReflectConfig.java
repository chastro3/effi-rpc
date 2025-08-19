package io.effi.rpc.nativetools;

import io.effi.rpc.compile.CompileTimeHelper;
import io.effi.rpc.constant.EffiRpcFramework;
import io.effi.rpc.util.StringUtil;

import javax.lang.model.type.MirroredTypeException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a configuration for reflection in native config.
 *
 * @see <a href="https://github.com/oracle/graal/blob/master/docs/reference-manual/native-image/assets/reflect-config-schema-v1.1.0.json">reflect-config-schema-v1.1.0.json</a>
 */
public class ReflectConfig implements NativeConfig<List<Map<String, Object>>> {

    private final List<Item> items = new ArrayList<>();

    public ReflectConfig addItem(Item item) {
        if (item != null) items.add(item);
        return this;
    }

    @Override
    public String name() {
        return "reflect-config.json";
    }

    @Override
    public List<Map<String, Object>> toJsonConfig() {
        return NativeConfig.Item.toMapList(items);
    }

    @Override
    public boolean hasResource() {
        return !items.isEmpty();
    }

    /**
     * Represents a reflection configuration item in reflect-config.
     */
    public static class Item implements NativeConfig.Item {

        /**
         * Optional. Only register when this class is reachable.
         */
        private ConditionItem condition;

        /**
         * Required. Type descriptor of the class ("type" property).
         */
        private String type;

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

        public static Item from(NativeConfig.Reflect reflect, CompileTimeHelper helper) {
            String typeReachedName = getTypeReachedName(reflect, helper);
            Item item = new Item();
            if (StringUtil.isNotBlank(typeReachedName)) {
                ConditionItem conditionItem = new ConditionItem().typeReached(typeReachedName);
                item.condition(conditionItem);
            }
            return item.allDeclaredClasses(reflect.allDeclaredClasses())
                    .allDeclaredMethods(reflect.allDeclaredMethods())
                    .allDeclaredFields(reflect.allDeclaredFields())
                    .allDeclaredConstructors(reflect.allDeclaredConstructors())
                    .allPublicClasses(reflect.allPublicClasses())
                    .allPublicMethods(reflect.allPublicMethods())
                    .allPublicFields(reflect.allPublicFields())
                    .allPublicConstructors(reflect.allPublicConstructors())
                    .allRecordComponents(reflect.allRecordComponents())
                    .allPermittedSubclasses(reflect.allPermittedSubclasses())
                    .allNestMembers(reflect.allNestMembers())
                    .allSigners(reflect.allSigners())
                    .queryAllDeclaredMethods(reflect.queryAllDeclaredMethods())
                    .queryAllDeclaredConstructors(reflect.queryAllDeclaredConstructors())
                    .queryAllPublicMethods(reflect.queryAllPublicMethods())
                    .queryAllPublicConstructors(reflect.queryAllPublicConstructors())
                    .unsafeAllocated(reflect.unsafeAllocated());
        }

        public static String getTypeReachedName(NativeConfig.Reflect reflect, CompileTimeHelper helper) {
            String typeReachedName = reflect.typeReachedName();
            if (StringUtil.isNotBlank(typeReachedName)) return typeReachedName;
            try {
                Class<?> typeReached = reflect.typeReached();
                return typeReached.getName();
            } catch (MirroredTypeException e) {
                return helper.qualifiedNameOf(helper.asType(e.getTypeMirror()));
            }
        }

        public Item condition(ConditionItem condition) {
            this.condition = condition;
            return this;
        }

        public Item type(String type) {
            this.type = type;
            return this;
        }

        public Item method(String name, List<String> parameterTypes) {
            methods.add(new MethodConfigItem(name, parameterTypes));
            return this;
        }

        public Item queriedMethod(String name, List<String> parameterTypes) {
            queriedMethods.add(new MethodConfigItem(name, parameterTypes));
            return this;
        }

        public Item fields(List<String> fields) {
            this.fields = fields;
            return this;
        }

        public Item allDeclaredClasses(Boolean v) {
            this.allDeclaredClasses = v;
            return this;
        }

        public Item allDeclaredMethods(Boolean v) {
            this.allDeclaredMethods = v;
            return this;
        }

        public Item allDeclaredFields(Boolean v) {
            this.allDeclaredFields = v;
            return this;
        }

        public Item allDeclaredConstructors(Boolean v) {
            this.allDeclaredConstructors = v;
            return this;
        }

        public Item allPublicClasses(Boolean v) {
            this.allPublicClasses = v;
            return this;
        }

        public Item allPublicMethods(Boolean v) {
            this.allPublicMethods = v;
            return this;
        }

        public Item allPublicFields(Boolean v) {
            this.allPublicFields = v;
            return this;
        }

        public Item allPublicConstructors(Boolean v) {
            this.allPublicConstructors = v;
            return this;
        }

        public Item allRecordComponents(Boolean v) {
            this.allRecordComponents = v;
            return this;
        }

        public Item allPermittedSubclasses(Boolean v) {
            this.allPermittedSubclasses = v;
            return this;
        }

        public Item allNestMembers(Boolean v) {
            this.allNestMembers = v;
            return this;
        }

        public Item allSigners(Boolean v) {
            this.allSigners = v;
            return this;
        }

        public Item queryAllDeclaredMethods(Boolean v) {
            this.queryAllDeclaredMethods = v;
            return this;
        }

        public Item queryAllDeclaredConstructors(Boolean v) {
            this.queryAllDeclaredConstructors = v;
            return this;
        }

        public Item queryAllPublicMethods(Boolean v) {
            this.queryAllPublicMethods = v;
            return this;
        }

        public Item queryAllPublicConstructors(Boolean v) {
            this.queryAllPublicConstructors = v;
            return this;
        }

        public Item unsafeAllocated(Boolean v) {
            this.unsafeAllocated = v;
            return this;
        }

        @Override
        public Map<String, Object> toMap() {
            int javaVersion = EffiRpcFramework.javaVersion();
            return MapBuilder.create(4)
                    .put("condition", condition)
                    .putIf(javaVersion > 22, "type", type)
                    .putIf(javaVersion < 23, "name", type)
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

        static class MethodConfigItem implements NativeConfig.Item {

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
}
