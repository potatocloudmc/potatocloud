package net.potatocloud.webinterface.security;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.websocket.WsContext;
import lombok.RequiredArgsConstructor;
import net.potatocloud.common.config.Config;
import net.potatocloud.webinterface.dto.event.ErrorDto;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;

import java.security.Key;

@RequiredArgsConstructor
public class AuthService {

    private final Config config;

    public void authorizeHttp(Context ctx) {
        if (!config.get("require-auth").asBoolean()) {
            return;
        }

        if (ctx.path().endsWith("/live")) {
            return;
        }

        if (isAuthorizedToken(resolveBearer(ctx.header("Authorization")))) {
            return;
        }

        ctx.status(HttpStatus.UNAUTHORIZED).json(ErrorDto.builder().error("Unauthorized").build());
        ctx.skipRemainingHandlers();
    }

    public boolean authorizeWs(WsContext ctx) {
        if (!config.get("require-auth").asBoolean()) {
            return true;
        }

        String token = resolveBearer(ctx.header("Authorization"));
        if (token.isEmpty()) {
            token = defaultString(ctx.queryParam("apiKey"));
        }

        return isValidJwtToken(token);
    }

    private boolean isValidJwtToken(String token) {
        try {
            String jwtSecret = config.get("jwt-secret").asString();

            Key signingKey = new javax.crypto.spec.SecretKeySpec(jwtSecret.getBytes(), "HmacSHA256");
            JwtConsumer consumer = new JwtConsumerBuilder()
                    .setRequireExpirationTime()
                    .setVerificationKey(signingKey)
                    .build();

            JwtClaims claims = consumer.processToClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean isAuthorizedToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        return config.get("api-keys").asStringList().stream().anyMatch(key -> constantTimeEquals(key, token));
    }

    private String resolveBearer(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            return "";
        }
        return header.substring("Bearer ".length()).trim();
    }

    private String defaultString(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            int dummy = 0;
            for (int i = 0; i < a.length(); i++) {
                dummy |= a.charAt(i) ^ 'x';
            }
            return false;
        }

        int diff = 0;
        for (int i = 0; i < a.length(); i++) {
            diff |= a.charAt(i) ^ b.charAt(i);
        }
        return diff == 0;
    }
}
