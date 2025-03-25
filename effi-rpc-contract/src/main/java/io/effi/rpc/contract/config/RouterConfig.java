package io.effi.rpc.contract.config;

import org.intellij.lang.annotations.Language;

/**
 * Router config.
 */
public class RouterConfig {

    private final String urlRegex;

    private String matchTargetRegex;

    public RouterConfig(@Language("RegExp") String urlRegex) {
        this.urlRegex = urlRegex;
    }

    /**
     * Returns the urlRegex.
     *
     * @return the urlRegex
     */
    public String urlRegex() {
        return urlRegex;
    }

    /**
     * Returns the matchTargetRegex.
     *
     * @return the matchTargetRegex
     */
    public String matchTargetRegex() {
        return matchTargetRegex;
    }

    /**
     * Match target regex.
     *
     * @param matchTargetRegex
     * @return
     */
    public RouterConfig match(@Language("RegExp") String matchTargetRegex) {
        this.matchTargetRegex = matchTargetRegex;
        return this;
    }
}
