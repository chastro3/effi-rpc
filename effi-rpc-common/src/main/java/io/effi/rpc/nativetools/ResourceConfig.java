package io.effi.rpc.nativetools;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ResourceConfig implements NativeConfig<Map<String, Object>> {

    private final Resources resources = new Resources();

    public ResourceConfig addInclude(String conditionClass, String pattern) {
        resources.includes.add(new ResourcesItem(conditionClass, pattern));
        return this;
    }

    public ResourceConfig addExclude(String conditionClass, String pattern) {
        resources.excludes.add(new ResourcesItem(conditionClass, pattern));
        return this;
    }

    @Override
    public String name() {
        return "resource-config.json";
    }

    @Override
    public Map<String, Object> toJsonConfig() {
        Map<String, Object> map = new LinkedHashMap<>(2);
        map.put("resources", resources.toMap());
        return map;
    }

    @Override
    public boolean hasResource() {
        return !resources.excludes.isEmpty() || !resources.includes.isEmpty();
    }

    static class Resources implements Item {

        private final List<ResourcesItem> includes = new ArrayList<>();

        private final List<ResourcesItem> excludes = new ArrayList<>();

        @Override
        public Map<String, Object> toMap() {
            if (!includes.isEmpty() || !excludes.isEmpty()) {
                Map<String, Object> map = new LinkedHashMap<>(2);
                if (!includes.isEmpty()) {
                    map.put("includes", includes());
                }
                if (!excludes.isEmpty()) {
                    map.put("excludes", excludes());
                }
                return map;
            }
            return Map.of();
        }

        private List<Map<String, Object>> includes() {
            return includes.stream().map(ResourcesItem::toMap).toList();
        }

        private List<Map<String, Object>> excludes() {
            return excludes.stream().map(ResourcesItem::toMap).toList();
        }
    }

    record ResourcesItem(String conditionClass, String pattern) implements Item {

        @Override
        public Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>(2);
            if (conditionClass != null)
                map.put("condition", Map.of("typeReachable", conditionClass));
            if (pattern != null) {
                map.put("pattern", pattern);
            }
            return map;
        }
    }

}
