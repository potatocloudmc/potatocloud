package net.potatocloud.webinterface.security;

import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.AuthenticationRequest;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Set;

@ApplicationScoped
public class AuthenticationMechanism implements HttpAuthenticationMechanism {

    @Inject
    IdentityProviderManager identityProviderManager;

    @Override
    public Uni<SecurityIdentity> authenticate(RoutingContext context,
                                              IdentityProviderManager identityProviderManager) {
        String apiKey = context.request().getHeader("X-API-Key");
        if (apiKey != null && !apiKey.isBlank()) {
            return identityProviderManager
                    .authenticate(new ApiKeyAuthenticationRequest(apiKey));
        }

        String authHeader = context.request().getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return identityProviderManager
                    .authenticate(new JwtAuthenticationRequest(token));
        }

        String wsProtocol = context.request().getHeader("Sec-WebSocket-Protocol");
        if (wsProtocol != null) {
            String[] parts = wsProtocol.split(",\\s*");
            if (parts.length == 2 && "bearer".equals(parts[0])) {
                return identityProviderManager
                        .authenticate(new JwtAuthenticationRequest(parts[1]));
            }
        }

        return Uni.createFrom().nullItem();
    }

    @Override
    public Uni<ChallengeData> getChallenge(RoutingContext context) {
        return Uni.createFrom().item(
                new ChallengeData(401, "WWW-Authenticate", "Bearer")
        );
    }

    @Override
    public Set<Class<? extends AuthenticationRequest>> getCredentialTypes() {
        return Set.of(ApiKeyAuthenticationRequest.class, JwtAuthenticationRequest.class);
    }
}