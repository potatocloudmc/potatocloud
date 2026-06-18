package net.potatocloud.webinterface.security;

import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.AuthenticationRequest;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Set;

/**
 * Resolves an API key from the request and turns it into a SecurityIdentity
 * via {@link ApiKeyIdentityProvider}.
 * <p>
 * Lookup order:
 * 1. Header configured by app.security.api-key.header (default X-API-Key), used by normal REST clients.
 * 2. Query parameter "apiKey", used for the WebSocket handshake, since
 * browser WebSocket clients cannot set custom request headers.
 * <p>
 * Returning null from getChallenge()/authenticate() simply means "this
 * mechanism found no credentials"; Quarkus then returns 401 for endpoints
 * that require authentication via @Authenticated.
 */
@ApplicationScoped
public class ApiKeyAuthenticationMechanism implements HttpAuthenticationMechanism {

    @ConfigProperty(name = "app.security.api-key.header", defaultValue = "X-API-Key")
    String headerName;

    @Override
    public Uni<SecurityIdentity> authenticate(RoutingContext context, IdentityProviderManager identityProviderManager) {
        String apiKey = extractApiKey(context);
        if (apiKey == null || apiKey.isBlank()) {
            return Uni.createFrom().nullItem();
        }
        return identityProviderManager.authenticate(new ApiKeyAuthenticationRequest(apiKey));
    }

    @Override
    public Uni<ChallengeData> getChallenge(RoutingContext context) {
        ChallengeData challengeData = new ChallengeData(
                401,
                "WWW-Authenticate",
                "ApiKey realm=\"app\"");
        return Uni.createFrom().item(challengeData);
    }

    @Override
    public Set<Class<? extends AuthenticationRequest>> getCredentialTypes() {
        return Set.of(ApiKeyAuthenticationRequest.class);
    }

    private String extractApiKey(RoutingContext context) {
        HttpServerRequest request = context.request();
        String header = request.getHeader(headerName);
        if (header != null && !header.isBlank()) {
            return header;
        }
        return request.getParam("apiKey");
    }
}
