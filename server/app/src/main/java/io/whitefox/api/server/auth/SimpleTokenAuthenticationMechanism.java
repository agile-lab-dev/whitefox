package io.whitefox.api.server.auth;

import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.AnonymousAuthenticationRequest;
import io.quarkus.security.identity.request.AuthenticationRequest;
import io.quarkus.security.runtime.QuarkusPrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.inject.Inject;

import java.util.Set;

public class SimpleTokenAuthenticationMechanism implements HttpAuthenticationMechanism {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final QuarkusPrincipal principal = new QuarkusPrincipal("Mr. WhiteFox");

    String token;

    @Inject
    IdentityProviderManager identityProvider;


    public SimpleTokenAuthenticationMechanism(String token) {
        this.token = token;
    }

    private Uni<SecurityIdentity> anonymous() {
        return identityProvider.authenticate(AnonymousAuthenticationRequest.INSTANCE);
    }

    @Override
    public Uni<SecurityIdentity> authenticate(RoutingContext context, IdentityProviderManager identityProviderManager) {
        if (context.normalizedPath().startsWith("/q/"))
            return anonymous();
        var identity =
                new QuarkusSecurityIdentity.Builder()
                        .setPrincipal(principal)
                        .build();
        if (context.request().headers().get(AUTHORIZATION_HEADER).equals("Bearer " + token))
            return Uni.createFrom().item(identity);
        else
            throw new AuthenticationFailedException("Missing or unrecognized credentials");
    }

    @Override
    public Uni<ChallengeData> getChallenge(RoutingContext context) {
        return null;
    }

    @Override
    public Set<Class<? extends AuthenticationRequest>> getCredentialTypes() {
        return null;
    }
}
