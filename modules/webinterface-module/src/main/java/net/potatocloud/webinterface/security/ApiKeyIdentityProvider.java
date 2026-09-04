package net.potatocloud.webinterface.security;

import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.security.Principal;

/**
 * An IdentityProvider that authenticates API keys provided in the X-API-Key header.
 * Valid API keys are configured via the "app.security.api-keys" property, which should contain a comma-separated list of valid keys. If a provided API key matches one of the valid keys, a SecurityIdentity with the "client" role is returned. Otherwise, authentication fails.
 */
@ApplicationScoped
public class ApiKeyIdentityProvider implements IdentityProvider<ApiKeyAuthenticationRequest> {

    @ConfigProperty(name = "app.security.api-keys", defaultValue = "")
    String apiKeysConfig;

    /**
     * Specifies that this IdentityProvider supports authentication requests of type ApiKeyAuthenticationRequest.
     *
     * @return The class type of the supported authentication request
     */
    @Override
    public Class<ApiKeyAuthenticationRequest> getRequestType() {
        return ApiKeyAuthenticationRequest.class;
    }

    /**
     * Authenticates the provided API key against the configured valid keys. If a match is found, a SecurityIdentity with the "client" role is returned. Otherwise, authentication fails.
     *
     * @param request The authentication request containing the API key to authenticate
     * @param context The context of the request, which can be used to access additional information about the authentication process
     * @return A Uni that emits the authenticated SecurityIdentity if authentication is successful, or null if authentication fails
     */
    @Override
    public Uni<SecurityIdentity> authenticate(ApiKeyAuthenticationRequest request, AuthenticationRequestContext context) {
        return Uni.createFrom().item(() -> {
            String provided = request.apiKey();
            String[] validKeys = apiKeysConfig.split(",");
            for (String validKey : validKeys) {
                if (constantTimeEquals(provided, validKey)) {
                    Principal principal = () -> "api-key-client";
                    return QuarkusSecurityIdentity.builder()
                            .setPrincipal(principal)
                            .addRole("client")
                            .build();
                }
            }
            return null;
        });
    }

    /**
     * Compares two strings in constant time to prevent timing attacks.
     *
     * @param a
     * @param b
     * @return
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        byte[] aBytes = a.getBytes();
        byte[] bBytes = b.getBytes();
        int diff = aBytes.length ^ bBytes.length;
        int max = Math.max(aBytes.length, bBytes.length);
        for (int i = 0; i < max; i++) {
            byte aByte = i < aBytes.length ? aBytes[i] : 0;
            byte bByte = i < bBytes.length ? bBytes[i] : 0;
            diff |= aByte ^ bByte;
        }
        return diff == 0;
    }
}
