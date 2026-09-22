package net.potatocloud.webinterface.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class JwtIdentityProvider implements IdentityProvider<JwtAuthenticationRequest> {

    @ConfigProperty(name = "app.security.jwt-secret")
    String jwtSecret;

    @Override
    public Class<JwtAuthenticationRequest> getRequestType() {
        return JwtAuthenticationRequest.class;
    }

    @Override
    public Uni<SecurityIdentity> authenticate(JwtAuthenticationRequest request,
                                              AuthenticationRequestContext context) {
        return Uni.createFrom().item(() -> {
            try {
                SecretKey key = Keys.hmacShaKeyFor(
                        jwtSecret.getBytes(StandardCharsets.UTF_8)
                );

                Claims claims = Jwts.parser()
                        .verifyWith(key)
                        .build()
                        .parseSignedClaims(request.getToken())
                        .getPayload();

                String username = claims.get("username", String.class);

                return QuarkusSecurityIdentity.builder()
                        .setPrincipal(() -> username)
                        .addRole("client")
                        .build();

            } catch (JwtException e) {
                return null;
            }
        });
    }
}