package io.whitefox.api.server.auth;

import io.quarkus.runtime.Startup;
import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.AnonymousAuthenticationRequest;
import io.quarkus.security.runtime.QuarkusPrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticator;
import io.quarkus.vertx.http.runtime.security.PathMatchingHttpSecurityPolicy;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.Priority;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.security.Principal;
import java.util.Set;
import java.util.logging.Logger;


/**
 * A custom {@link HttpAuthenticator}. This authenticator that performs the following main duties:
 *
 * <ul>
 *   <li>Prevents the Quarkus OIDC authentication mechanism from attempting authentication when it
 *       is not configured. Note that attempts to use the OIDC authentication mechanism when the
 *       authentication server is not properly configured will result in 500 errors as opposed to
 *       401 (not authorized).
 *   <li>Completely disallows unauthenticated requests when authentication is enabled.
 * </ul>
 */

@Alternative // @Alternative + @Priority ensure the original HttpAuthenticator bean is not used
@Priority(1)
@Singleton
public class WhitefoxHttpAuthenticator extends HttpAuthenticator {

    private static final Logger logger = Logger.getLogger(WhitefoxHttpAuthenticator.class.getName());

    @Inject
    IdentityProviderManager identityProvider;

    private final Set<String> anonymousPaths;
    String token;
    private final boolean authEnabled;


    @Inject
    public WhitefoxHttpAuthenticator(
            WhitefoxAuthenticationConfig config,
            IdentityProviderManager identityProviderManager,
            Instance<PathMatchingHttpSecurityPolicy> pathMatchingPolicy,
            Instance<HttpAuthenticationMechanism> httpAuthenticationMechanism,
            Instance<IdentityProvider<?>> providers) {
        super(identityProviderManager, pathMatchingPolicy, httpAuthenticationMechanism, providers);
        this.identityProvider = identityProviderManager;
        authEnabled = config.enabled();
        token = config.bearerToken();
        anonymousPaths = config.anonymousPaths();
    }

    @Override
    public Uni<SecurityIdentity> attemptAuthentication(RoutingContext context) {
        if (!authEnabled) {
            return anonymous();
        }
        else if (context.normalizedPath().startsWith("/q/") || anonymousPaths.contains(context.normalizedPath())){
            return anonymous();
        }
        else if (token != null){
            var principal = new QuarkusPrincipal("Mr. WhiteFox");
            var identity =
                    new QuarkusSecurityIdentity.Builder()
                            .setPrincipal(principal)
                            .addRole("user")
                            .build();
            if (context.request().headers().get("Authorization").equals("Bearer " + token))
                return Uni.createFrom().item(identity);
            else
                throw new AuthenticationFailedException("Missing or unrecognized credentials");

        }
        else
            throw new AuthenticationFailedException("Missing or unrecognized credentials");
//        return super.attemptAuthentication(context)
//                .onItem()
//                .transform(
//                        securityIdentity -> {
//                            if (securityIdentity == null) {
//                                // Disallow unauthenticated requests when requested by configuration.
//                                // Note: Quarkus by default permits unauthenticated requests unless there are
//                                // specific authorization rules that validate the security identity.
//                                throw new AuthenticationFailedException("Missing or unrecognized credentials");
//                            }
//
//                            return securityIdentity;
//                        });
    }

    private Uni<SecurityIdentity> anonymous() {
        return identityProvider.authenticate(AnonymousAuthenticationRequest.INSTANCE);
    }
}
