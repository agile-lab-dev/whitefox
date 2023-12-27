package io.whitefox.api.server.auth;

import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;

public interface AuthenticationService {
    Uni<SecurityIdentity> authenticate(RoutingContext context);
}
