package io.whitefox.api.server.auth;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

import java.util.Set;

/** Configuration for Whitefox authentication settings. */
@ConfigMapping(prefix = "whitefox.server.authentication")
public interface WhitefoxAuthenticationConfig {

    /** Returns {@code true} if Whitefox authentication is enabled. */
    @WithName("enabled")
    @WithDefault("false")
    boolean enabled();

    @WithName("bearerToken")
    String bearerToken();

    /** Returns the set of HTTP URL paths that are permitted to be serviced without authentication. */
    @WithName("anonymous-paths")
    Set<String> anonymousPaths();
}
