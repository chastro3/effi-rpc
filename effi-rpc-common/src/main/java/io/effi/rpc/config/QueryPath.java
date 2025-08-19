package io.effi.rpc.config;

import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses URL path segments and query parameters.
 * <p>
 * Provides utilities for handling URL paths with variable substitution,
 * path matching, and query parameter parsing and rendering.
 */
public class QueryPath {

    private static final QueryPath EMPTY = QueryPath.valueOf(StringUtil.empty());

    private final String path;
    private final String[] pathSegments;
    private final PathVariable[] pathVariables;
    private final Map<String, String> queryParams;

    QueryPath(String path, Map<String, String> params) {
        this.pathSegments = splitPath(path);
        this.path = String.join("/", pathSegments);
        this.pathVariables = extractPathVariable(pathSegments);
        this.queryParams = params;
    }

    public static QueryPath empty() {
        return EMPTY;
    }

    public static QueryPath valueOf(Collection<String> pathSegments) {
        return valueOf(String.join("/", pathSegments));
    }

    /**
     * Creates a QueryPath instance from a URL string.
     * Returns empty instance if input is blank.
     */
    public static QueryPath valueOf(String input) {
        String path = null;
        Map<String, String> params = null;
        if (StringUtil.isBlank(input)) {
            return new QueryPath(path, params);
        }
        int questionMarkIndex = input.indexOf('?');
        if (questionMarkIndex == -1) {
            // Treat the whole input as query parameters if it contains '='
            if (input.contains("=")) {
                params = URLUtil.parseQueryParam(input);
            } else {
                path = input;
            }
        } else {
            // Split into path and parameter parts
            path = input.substring(0, questionMarkIndex);
            String paramsPart = input.substring(questionMarkIndex + 1);
            if (!paramsPart.isEmpty()) {
                params = URLUtil.parseQueryParam(paramsPart);
            }
        }
        return new QueryPath(path, params);
    }

    /**
     * Replaces path variables with given values and returns rendered segments.
     * Throws if any variable is missing.
     *
     * @param variables the map of variable names to replacement values
     * @return the rendered path segments array
     */

    public String[] render(Map<String, String> variables) {
        if (CollectionUtil.isEmpty(pathSegments)) return StringUtil.emptyArray();
        String[] result = Arrays.copyOf(pathSegments, pathSegments.length);
        if (hasPathVariable())
            for (PathVariable var : pathVariables) {
                String value = variables.get(var.name);
                if (value == null) {
                    throw new IllegalArgumentException("Missing path variable: " + var.name);
                }
                result[var.index] = value;
            }
        return result;
    }

    public boolean matched(String realPath) {
        return match(realPath) != null;
    }

    /**
     * Matches actual path segments against template segments, extracts variables,
     * and returns variable map or null if not matched.
     *
     * @param realPath the actual path string to match, e.g., "/user/123/profile"
     * @return the map of variable names to values if matched, empty map if no variables,
     * or null if path doesn't match template
     */
    public Map<String, String> match(String realPath) {
        String[] actualSegments = splitPath(realPath);
        // Number of segments must be equal to match
        if (actualSegments.length != pathSegments.length) {
            return null;
        }
        Map<String, String> result = new HashMap<>();
        if (hasPathVariable()) {
            for (int i = 0; i < pathSegments.length; i++) {
                String templateSegment = pathSegments[i];
                String actualSegment = actualSegments[i];
                // Check if this segment is a variable
                PathVariable var = lookupPathVariable(i);
                if (var != null) {
                    // Variable segment: bind actual segment to variable name
                    result.put(var.name, actualSegment);
                } else {
                    // Fixed segment: must exactly match
                    if (!templateSegment.equals(actualSegment)) {
                        return null;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Splits path into non-empty segments.
     */
    private String[] splitPath(String path) {
        if (StringUtil.isBlank(path)) {
            return StringUtil.emptyArray();
        }
        String[] parts = path.split("/");
        List<String> list = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(parts)) {
            for (String part : parts) {
                if (StringUtil.isNotBlank(part)) {
                    list.add(part);
                }
            }
        }
        return list.isEmpty()
                ? StringUtil.emptyArray()
                : list.toArray(StringUtil.emptyArray());
    }

    public String path() {
        return path;
    }

    public boolean hasPathVariable() {
        return pathVariables != null && pathVariables.length > 0;
    }

    public String[] pathSegments() {
        return pathSegments;
    }

    public Map<String, String> queryParams() {
        return queryParams;
    }

    @Override
    public String toString() {
        String path = path();
        String queryParam = URLUtil.toQueryParam(queryParams);
        if (StringUtil.isBlank(path)) {
            return queryParam;
        } else {
            return path + (StringUtil.isBlank(queryParam) ? "" : ("?" + queryParam));
        }
    }

    /**
     * Extracts variable name from path variable segment.
     */
    private PathVariable[] extractPathVariable(String[] pathSegments) {
        if (CollectionUtil.isEmpty(pathSegments)) {
            return null;
        }
        int count = 0;
        for (String seg : pathSegments) {
            if (isPathVariable(seg)) {
                count++;
            }
        }
        if (count == 0) return null;
        PathVariable[] result = new PathVariable[count];
        int idx = 0;
        for (int i = 0; i < pathSegments.length; i++) {
            String segment = pathSegments[i];
            if (isPathVariable(segment)) {
                result[idx++] = new PathVariable(i, extractPathVariableName(segment));
            }
        }
        return result;
    }


    private boolean isPathVariable(String segment) {
        return segment.startsWith("{") && segment.endsWith("}");
    }

    private String extractPathVariableName(String segment) {
        return segment.substring(1, segment.length() - 1).trim();
    }

    private PathVariable lookupPathVariable(int index) {
        if (!hasPathVariable()) return null;
        for (PathVariable var : pathVariables) {
            if (var.index == index) {
                return var;
            }
        }
        return null;
    }

    private record PathVariable(int index, String name) {
    }
}



