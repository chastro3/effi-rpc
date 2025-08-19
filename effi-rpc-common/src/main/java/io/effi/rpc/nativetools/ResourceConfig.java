package io.effi.rpc.nativetools;

import io.effi.rpc.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents a configuration for resource in native config.
 *
 * @see <a href="https://github.com/oracle/graal/blob/master/docs/reference-manual/native-image/assets/resource-config-schema-v1.1.0.json">resource-config-schema-v1.1.0.json</a>
 */
public class ResourceConfig implements NativeConfig<Map<String, Object>> {

    private final ResourcesItem resources = new ResourcesItem();

    private final List<GlobItem> globs = new ArrayList<>();

    private final List<BundleItem> bundles = new ArrayList<>();

    public ResourceConfig addIncludedResource(ConditionItem condition, String pattern) {
        if (StringUtil.isNotBlank(pattern)) {
            resources.includes.add(new ResourcesChildItem(condition, pattern));
        }
        return this;
    }

    public ResourceConfig addExcludeResource(ConditionItem condition, String pattern) {
        if (StringUtil.isNotBlank(pattern)) {
            resources.excludes.add(new ResourcesChildItem(condition, pattern));
        }
        return this;
    }

    public ResourceConfig addGlob(ConditionItem condition, String glob, String module) {
        globs.add(new GlobItem(condition, glob, module));
        return this;
    }

    public ResourceConfig addBundle(ConditionItem condition, String name, List<String> locales, List<String> classNames) {
        bundles.add(new BundleItem(condition, name, locales, classNames));
        return this;
    }

    @Override
    public String name() {
        return "resource-config.json";
    }

    @Override
    public Map<String, Object> toJsonConfig() {
        return MapBuilder.create(3)
                .put("resources", resources)
                .put("globs", globs)
                .put("bundles", bundles)
                .build();
    }

    @Override
    public boolean hasResource() {
        return !resources.excludes.isEmpty() || !resources.includes.isEmpty();
    }

    static class ResourcesItem implements NativeConfig.Item {

        private final List<ResourcesChildItem> includes = new ArrayList<>();

        private final List<ResourcesChildItem> excludes = new ArrayList<>();

        @Override
        public Map<String, Object> toMap() {
            if (!includes.isEmpty() || !excludes.isEmpty()) {
                return MapBuilder.create(2)
                        .put("includes", includes)
                        .put("excludes", excludes)
                        .build();
            }
            return Collections.emptyMap();
        }
    }

    record ResourcesChildItem(ConditionItem condition, String pattern) implements NativeConfig.Item {

        @Override
        public Map<String, Object> toMap() {
            return MapBuilder.create(2)
                    .put("condition", condition)
                    .put("pattern", pattern)
                    .build();
        }
    }

    record GlobItem(ConditionItem condition, String glob, String module) implements NativeConfig.Item {

        @Override
        public Map<String, Object> toMap() {
            return MapBuilder.create(3)
                    .put("condition", condition)
                    .put("glob", glob)
                    .put("module", module)
                    .build();
        }
    }

    record BundleItem(ConditionItem condition, String name, List<String> locales,
                      List<String> classNames) implements NativeConfig.Item {

        @Override
        public Map<String, Object> toMap() {
            return MapBuilder.create(4)
                    .put("condition", condition)
                    .put("name", name)
                    .put("locales", locales)
                    .put("classNames", classNames)
                    .build();
        }
    }

}
