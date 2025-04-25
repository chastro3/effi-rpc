package io.effi.rpc.config;

import io.effi.rpc.util.StringUtil;

import java.util.List;
import java.util.Map;

/**
 * Represents a parsed URL query path, including path segments and query parameters.
 */
public class QueryPath {

    public static final QueryPath EMPTY_PATH = QueryPath.valueOf("");

    private final List<String> paths;
    private final Map<String, String> queryParams;

    QueryPath(String path, Map<String, String> params) {
        this.paths = URLUtil.toPaths(path);
        this.queryParams = params;
    }

    /**
     * Creates a QueryPath instance from an input string.
     *
     * @param input the input URL string
     * @return a QueryPath object
     */
    public static QueryPath valueOf(String input) {
        return URLUtil.buildQueryPath(input);
    }

    public List<String> paths() {
        return paths;
    }

    public Map<String, String> queryParams() {
        return queryParams;
    }

    public String path() {
        return URLUtil.toPath(paths);
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
}



