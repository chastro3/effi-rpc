package io.effi.rpc.governance.router;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.registry.ServiceInstance;
import io.effi.rpc.util.AssertUtil;
import org.intellij.lang.annotations.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import static io.effi.rpc.annotation.component.ScopedComponent.Kind.SINGLE;
import static io.effi.rpc.annotation.component.ScopedComponent.Scope.MODULE;

/**
 * Defines ordered routing rules applied to discovered service instances.
 * <p>
 * Rules are evaluated in insertion order. The first rule whose URL pattern matches decides the
 * instance metadata filter. When no rule matches, every candidate passes through unchanged.
 */
@ScopedComponent(scope = MODULE, kind = SINGLE)
public final class RouterConfig {

    private final List<Rule> rules;

    private RouterConfig(Builder builder) {
        this.rules = builder.rules;
    }

    /**
     * Creates a router configuration builder.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the configured rules in evaluation order.
     *
     * @return the routing rules
     */
    public List<Rule> rules() {
        return rules;
    }

    /**
     * Matches a request URL and filters candidate instances by metadata.
     */
    public record Rule(Pattern url, Map<String, String> metadata) {

        public Rule {
            AssertUtil.notNull(url, "url");
            metadata = metadata == null ? Map.of() : metadata;
        }

        /**
         * Returns whether the request URL matches this rule.
         *
         * @param candidate the request URL
         * @return {@code true} when the URL matches
         */
        public boolean matchesUrl(String candidate) {
            return url.matcher(candidate).find();
        }

        /**
         * Returns whether the service instance satisfies every metadata condition.
         *
         * @param instance the candidate service instance
         * @return {@code true} when all metadata conditions match
         */
        public boolean matchesInstance(ServiceInstance instance) {
            return metadata.entrySet().stream()
                    .allMatch(entry -> Objects.equals(entry.getValue(), instance.metadata().get(entry.getKey())));
        }
    }

    /**
     * Builds an ordered list of routing rules.
     */
    public static final class Builder {

        private final List<Rule> rules = new ArrayList<>();

        private Builder() {
        }

        /**
         * Adds a URL-only routing rule.
         *
         * @param urlRegex the request URL pattern
         * @return this builder
         */
        public Builder rule(@Language("RegExp") String urlRegex) {
            return rule(urlRegex, Map.of());
        }

        /**
         * Adds a routing rule with URL and metadata conditions.
         *
         * @param urlRegex the request URL pattern
         * @param metadata the required instance metadata
         * @return this builder
         */
        public Builder rule(@Language("RegExp") String urlRegex, Map<String, String> metadata) {
            AssertUtil.notBlank(urlRegex, "urlRegex");
            rules.add(new Rule(Pattern.compile(urlRegex), metadata));
            return this;
        }

        /**
         * Adds a prebuilt routing rule.
         *
         * @param rule the routing rule
         * @return this builder
         */
        public Builder rule(Rule rule) {
            rules.add(AssertUtil.notNull(rule, "rule"));
            return this;
        }

        /**
         * Builds the router configuration.
         *
         * @return an immutable router configuration
         */
        public RouterConfig build() {
            return new RouterConfig(this);
        }
    }
}
