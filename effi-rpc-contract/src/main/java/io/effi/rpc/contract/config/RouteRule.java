package io.effi.rpc.contract.config;

import io.effi.rpc.common.url.URL;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.context.InvocationContext;

import java.util.List;

/**
 * Define a routing rule to filter and select target URLs that meet specified invocation conditions.
 */
@FunctionalInterface
public interface RouteRule {

    /**
     * Filters the list of URLs based on invocation details, returning URLs that meet the criteria.
     *
     * @param context the invocation context with request parameters and metadata
     * @param urls    the list of available URLs to be filtered
     * @return a list of URLs that match the routing rule; returns an empty list if none match
     */
    List<URL> execute(InvocationContext<?, Caller<?>> context, List<URL> urls);
}

